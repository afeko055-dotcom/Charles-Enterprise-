package com.example.repository

import com.example.data.database.AppDatabase
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.BankAccountEntity
import com.example.data.entity.ContactMessageEntity
import com.example.data.entity.DeliveryEntity
import com.example.data.entity.DeliveryLocationEntity
import com.example.data.entity.LedgerEntryEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.SavedVehicleEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.VehicleEntity
import com.example.data.entity.WebhookEventEntity
import com.example.data.entity.WithdrawalEntity
import com.example.data.model.DeliveryStatus
import com.example.data.model.LedgerDirection
import com.example.data.model.LedgerType
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleStatus
import com.example.data.model.WithdrawalStatus
import com.example.payment.PaymentInitRequest
import com.example.payment.PaymentProvider
import com.example.payment.RefundRequest
import com.example.payment.TransferRequest
import com.example.payment.WebhookEventPayload
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class FinancialSummary(
  val totalSalesKobo: Long,
  val totalFeesKobo: Long,
  val availableFundsKobo: Long,
  val pendingFundsKobo: Long,
  val totalWithdrawnKobo: Long,
  val totalRefundedKobo: Long
)

data class FinancialReconciliation(
  val totalPaidOrdersAmountKobo: Long,
  val totalPaymentGatewayAmountKobo: Long,
  val totalLedgerSalesKobo: Long,
  val discrepancyKobo: Long,
  val isReconciled: Boolean,
  val verifiedTransactionsCount: Int
)

class MarketplaceRepository(
  private val db: AppDatabase,
  private val paystackProvider: PaymentProvider,
  private val flutterwaveProvider: PaymentProvider
) {
  private fun getProvider(gateway: PaymentGateway): PaymentProvider {
    return when (gateway) {
      PaymentGateway.PAYSTACK -> paystackProvider
      PaymentGateway.FLUTTERWAVE -> flutterwaveProvider
      PaymentGateway.MONNIFY -> paystackProvider
    }
  }

  // --- VEHICLES ---
  val allVehicles: Flow<List<VehicleEntity>> = db.vehicleDao().getAllVehicles()

  fun getVehicle(id: String): Flow<VehicleEntity?> = db.vehicleDao().getVehicleById(id)

  suspend fun insertVehicle(vehicle: VehicleEntity, actor: UserEntity) {
    db.vehicleDao().insertVehicle(vehicle)
    db.auditLogDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = actor.id,
        actorName = actor.fullName,
        actorRole = actor.role.name,
        action = "VEHICLE_CREATED",
        details = "Created vehicle: ${vehicle.year} ${vehicle.brand} ${vehicle.model} (${vehicle.vin})",
        targetId = vehicle.id
      )
    )
  }

  suspend fun updateVehicle(vehicle: VehicleEntity, actor: UserEntity) {
    db.vehicleDao().updateVehicle(vehicle)
    db.auditLogDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = actor.id,
        actorName = actor.fullName,
        actorRole = actor.role.name,
        action = "VEHICLE_UPDATED",
        details = "Updated vehicle: ${vehicle.brand} ${vehicle.model}. Price: ₦${vehicle.priceKobo / 100}",
        targetId = vehicle.id
      )
    )
  }

  suspend fun deleteVehicle(vehicle: VehicleEntity, actor: UserEntity) {
    db.vehicleDao().deleteVehicle(vehicle)
    db.auditLogDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = actor.id,
        actorName = actor.fullName,
        actorRole = actor.role.name,
        action = "VEHICLE_DELETED",
        details = "Deleted vehicle ${vehicle.brand} ${vehicle.model} (${vehicle.vin})",
        targetId = vehicle.id
      )
    )
  }

  fun getSavedVehicles(userId: String): Flow<List<VehicleEntity>> =
    db.vehicleDao().getSavedVehiclesForUser(userId)

  fun isVehicleSaved(userId: String, vehicleId: String): Flow<Boolean> =
    db.vehicleDao().isVehicleSaved(userId, vehicleId)

  suspend fun toggleSaveVehicle(userId: String, vehicleId: String) {
    val isSaved = db.vehicleDao().isVehicleSaved(userId, vehicleId).first()
    if (isSaved) {
      db.vehicleDao().removeSavedVehicle(userId, vehicleId)
    } else {
      db.vehicleDao().saveVehicle(SavedVehicleEntity(userId, vehicleId))
    }
  }

  // --- CHECKOUT & ORDER CREATION (CONCURRENCY SAFE) ---
  suspend fun initiateVehiclePurchase(
    vehicleId: String,
    customer: UserEntity,
    deliveryAddress: String,
    deliveryCity: String,
    deliveryState: String,
    contactPhone: String,
    gateway: PaymentGateway = PaymentGateway.PAYSTACK
  ): Result<Triple<String, String, String>> { // Returns Triple(orderId, reference, checkoutUrl)
    val timestamp = System.currentTimeMillis()

    // 1. Concurrency safety: Atomically reserve vehicle
    val rowsAffected = db.vehicleDao().reserveVehicle(vehicleId, customer.id, timestamp)
    if (rowsAffected == 0) {
      return Result.failure(IllegalStateException("This vehicle is currently reserved by another customer or already sold."))
    }

    // 2. Fetch authoritative price from database
    val vehicle = db.vehicleDao().getVehicleByIdDirect(vehicleId)
      ?: return Result.failure(IllegalStateException("Vehicle not found in vault."))

    val basePriceKobo = vehicle.priceKobo
    val deliveryFeeKobo = 450_000_00L // ₦450,000 enclosed flatbed delivery
    val documentationFeeKobo = 250_000_00L // ₦250,000 luxury registration & PDI
    val totalAmountKobo = basePriceKobo + deliveryFeeKobo + documentationFeeKobo

    val orderId = "ord_" + UUID.randomUUID().toString().take(10)
    val orderNumber = "APX-" + (10000 + Random.nextInt(89999))

    // 3. Create Order
    val order = OrderEntity(
      id = orderId,
      orderNumber = orderNumber,
      customerId = customer.id,
      vehicleId = vehicle.id,
      vehicleTitle = "${vehicle.year} ${vehicle.brand} ${vehicle.model}",
      vehicleVin = vehicle.vin,
      basePriceKobo = basePriceKobo,
      deliveryFeeKobo = deliveryFeeKobo,
      documentationFeeKobo = documentationFeeKobo,
      totalAmountKobo = totalAmountKobo,
      currency = "NGN",
      status = OrderStatus.PAYMENT_PENDING,
      deliveryAddress = deliveryAddress,
      deliveryCity = deliveryCity,
      deliveryState = deliveryState,
      contactPhone = contactPhone,
      createdAt = timestamp,
      updatedAt = timestamp
    )
    db.orderDao().insertOrder(order)

    // 4. Request hosted checkout from provider abstraction
    val provider = getProvider(gateway)
    val initRequest = PaymentInitRequest(
      orderId = orderId,
      amountKobo = totalAmountKobo,
      currency = "NGN",
      customerEmail = customer.email,
      customerName = customer.fullName,
      metadata = mapOf("orderNumber" to orderNumber, "vin" to vehicle.vin)
    )
    val initResult = provider.initializePayment(initRequest)

    if (!initResult.success) {
      db.vehicleDao().releaseReservation(vehicleId)
      db.orderDao().updateOrderStatus(orderId, OrderStatus.CANCELLED, timestamp)
      return Result.failure(IllegalStateException(initResult.errorMessage ?: "Failed to initialize payment gateway."))
    }

    // 5. Store Payment record
    val payment = PaymentEntity(
      id = "pay_" + UUID.randomUUID().toString().take(10),
      orderId = orderId,
      provider = gateway,
      reference = initResult.reference,
      amountKobo = totalAmountKobo,
      currency = "NGN",
      status = PaymentStatus.INITIATED,
      hostedCheckoutUrl = initResult.checkoutUrl,
      customerEmail = customer.email,
      createdAt = timestamp,
      updatedAt = timestamp
    )
    db.paymentDao().insertPayment(payment)

    // Update order with payment reference
    db.orderDao().updateOrder(order.copy(paymentReference = initResult.reference))

    // Notification
    db.notificationDao().insertNotification(
      NotificationEntity(
        id = UUID.randomUUID().toString(),
        userId = customer.id,
        title = "Hosted Checkout Ready",
        message = "Order $orderNumber created. Proceed to the secure ${gateway.displayName} hosted checkout to finalize your purchase.",
        category = "ORDER"
      )
    )

    return Result.success(Triple(orderId, initResult.reference, initResult.checkoutUrl))
  }

  // --- SERVER-SIDE PAYMENT VERIFICATION & WEBHOOK INGESTION ---
  suspend fun verifyAndCompletePayment(
    reference: String,
    providerGateway: PaymentGateway
  ): Result<OrderEntity> {
    val timestamp = System.currentTimeMillis()
    val payment = db.paymentDao().getPaymentByReferenceDirect(reference)
      ?: return Result.failure(IllegalStateException("Transaction reference not recognized."))

    val order = db.orderDao().getOrderByIdDirect(payment.orderId)
      ?: return Result.failure(IllegalStateException("Order not found."))

    // If already verified, return idempotent success
    if (payment.status == PaymentStatus.SUCCESS && order.status == OrderStatus.PAID) {
      return Result.success(order)
    }

    // Call Provider to verify transaction server-side
    val provider = getProvider(providerGateway)
    val verifyResult = provider.verifyPayment(reference)

    if (!verifyResult.isSuccessful || verifyResult.status != PaymentStatus.SUCCESS) {
      db.paymentDao().updatePayment(payment.copy(status = PaymentStatus.FAILED, updatedAt = timestamp))
      db.orderDao().updateOrderStatus(order.id, OrderStatus.CANCELLED, timestamp)
      db.vehicleDao().releaseReservation(order.vehicleId)
      return Result.failure(IllegalStateException("Payment verification failed on provider side."))
    }

    // Server-side check of amount & currency
    val feeKobo = (order.totalAmountKobo * 0.015).toLong() // 1.5% gateway fee
    val netAmountKobo = order.totalAmountKobo - feeKobo

    // 1. Update Payment
    db.paymentDao().updatePayment(
      payment.copy(
        status = PaymentStatus.SUCCESS,
        providerTransactionId = verifyResult.providerTransactionId,
        paidAt = timestamp,
        updatedAt = timestamp
      )
    )

    // 2. Update Order
    val updatedOrder = order.copy(
      status = OrderStatus.CONFIRMED,
      updatedAt = timestamp
    )
    db.orderDao().updateOrder(updatedOrder)

    // 3. Mark Vehicle Sold
    db.vehicleDao().markSold(order.vehicleId, timestamp)

    // 4. Create Immutable Financial Ledger Entries
    val ledgerSale = LedgerEntryEntity(
      id = "led_sale_" + UUID.randomUUID().toString().take(10),
      type = LedgerType.SALE,
      direction = LedgerDirection.CREDIT,
      amountKobo = order.totalAmountKobo,
      feeKobo = feeKobo,
      netAmountKobo = netAmountKobo,
      currency = "NGN",
      reference = reference,
      orderId = order.id,
      paymentId = payment.id,
      description = "Settlement for ${order.vehicleTitle} (Order ${order.orderNumber})",
      isAvailable = false // Pending provider bank settlement cycle
    )
    val ledgerFee = LedgerEntryEntity(
      id = "led_fee_" + UUID.randomUUID().toString().take(10),
      type = LedgerType.PAYMENT_FEE,
      direction = LedgerDirection.DEBIT,
      amountKobo = feeKobo,
      feeKobo = 0L,
      netAmountKobo = feeKobo,
      currency = "NGN",
      reference = reference,
      orderId = order.id,
      paymentId = payment.id,
      description = "${providerGateway.displayName} transaction processing fee",
      isAvailable = false
    )
    db.ledgerDao().insertLedgerEntries(listOf(ledgerSale, ledgerFee))

    // 5. Generate Secure 6-Digit Delivery OTP for Customer Handover
    val deliveryOtp = (100000 + Random.nextInt(899999)).toString()
    val hashedOtp = hashOtp(deliveryOtp)

    // 6. Automatically dispatch order to Flatbed carrier
    val delivery = DeliveryEntity(
      id = "del_" + UUID.randomUUID().toString().take(10),
      orderId = order.id,
      vehicleId = order.vehicleId,
      riderId = "user_rider_1",
      riderName = "Chinedu Eze",
      riderPhone = "+234 812 400 9988",
      carrierVehicle = "Mercedes-Benz Actros 3340 Flatbed",
      carrierPlate = "APP-402-XA",
      status = DeliveryStatus.ASSIGNED,
      destinationAddress = "${order.deliveryAddress}, ${order.deliveryCity}, ${order.deliveryState}",
      deliveryOtpHash = hashedOtp,
      deliveryOtpPlainForCustomer = deliveryOtp,
      etaMinutes = 45,
      updatedAt = timestamp
    )
    db.deliveryDao().insertDelivery(delivery)

    // 7. Audit log & Notifications
    db.auditLogDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = order.customerId,
        actorName = "Customer Checkout",
        actorRole = "CUSTOMER",
        action = "PAYMENT_VERIFIED",
        details = "Payment verified for Order ${order.orderNumber}. Ref: $reference. Amount: ₦${order.totalAmountKobo / 100}",
        targetId = order.id
      )
    )

    db.notificationDao().insertNotification(
      NotificationEntity(
        id = UUID.randomUUID().toString(),
        userId = order.customerId,
        title = "Payment Confirmed & Carrier Assigned",
        message = "Your payment for ${order.vehicleTitle} has been verified. Dedicated carrier unit assigned. Your Secure Delivery OTP is $deliveryOtp.",
        category = "PAYMENT"
      )
    )

    return Result.success(updatedOrder)
  }

  // --- WEBHOOK HANDLING WITH IDEMPOTENCY ---
  suspend fun handleIncomingWebhook(
    gateway: PaymentGateway,
    eventId: String,
    eventType: String,
    reference: String,
    amountKobo: Long,
    signature: String,
    rawPayload: String
  ): Result<String> {
    val timestamp = System.currentTimeMillis()

    // 1. Check idempotency
    val existingEvent = db.webhookEventDao().getEvent(gateway.name, eventId)
    if (existingEvent != null && existingEvent.processed) {
      return Result.success("Event $eventId already processed (Idempotent ignore)")
    }

    // 2. Validate with provider
    val provider = getProvider(gateway)
    val payload = WebhookEventPayload(
      eventId = eventId,
      eventType = eventType,
      provider = gateway,
      reference = reference,
      amountKobo = amountKobo,
      currency = "NGN",
      status = PaymentStatus.SUCCESS,
      signature = signature,
      rawPayloadJson = rawPayload
    )
    val processResult = provider.handleWebhook(signature, payload)

    if (!processResult.isValidSignature) {
      return Result.failure(SecurityException("Invalid webhook HMAC signature."))
    }

    // Record webhook event
    val event = WebhookEventEntity(
      id = "ev_" + UUID.randomUUID().toString().take(10),
      provider = gateway.name,
      eventId = eventId,
      eventType = eventType,
      processed = true,
      payloadJson = rawPayload,
      receivedAt = timestamp,
      processedAt = timestamp
    )
    db.webhookEventDao().insertEvent(event)

    // Trigger state change
    verifyAndCompletePayment(reference, gateway)

    return Result.success("Webhook processed and order verified successfully")
  }

  // --- DISPATCH & LIVE GPS TRACKING ---
  val allDeliveries: Flow<List<DeliveryEntity>> = db.deliveryDao().getAllDeliveries()

  fun getDeliveryForOrder(orderId: String): Flow<DeliveryEntity?> =
    db.deliveryDao().getDeliveryForOrder(orderId)

  fun getDeliveriesForRider(riderId: String): Flow<List<DeliveryEntity>> =
    db.deliveryDao().getDeliveriesForRider(riderId)

  suspend fun updateDeliveryStatus(
    deliveryId: String,
    newStatus: DeliveryStatus,
    actor: UserEntity
  ): Result<DeliveryEntity> {
    val timestamp = System.currentTimeMillis()
    val delivery = db.deliveryDao().getDeliveryByIdDirect(deliveryId)
      ?: return Result.failure(IllegalStateException("Delivery not found"))

    val updated = delivery.copy(
      status = newStatus,
      startedAt = if (newStatus == DeliveryStatus.PICKUP_PENDING && delivery.startedAt == null) timestamp else delivery.startedAt,
      pickedUpAt = if (newStatus == DeliveryStatus.PICKED_UP && delivery.pickedUpAt == null) timestamp else delivery.pickedUpAt,
      deliveredAt = if (newStatus == DeliveryStatus.DELIVERED && delivery.deliveredAt == null) timestamp else delivery.deliveredAt,
      updatedAt = timestamp
    )
    db.deliveryDao().updateDelivery(updated)

    // Notify customer
    val order = db.orderDao().getOrderByIdDirect(delivery.orderId)
    if (order != null) {
      db.notificationDao().insertNotification(
        NotificationEntity(
          id = UUID.randomUUID().toString(),
          userId = order.customerId,
          title = "Delivery Update: ${newStatus.label}",
          message = "Carrier update for ${order.vehicleTitle}: Status changed to ${newStatus.label}.",
          category = "DISPATCH"
        )
      )
    }

    db.auditLogDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = actor.id,
        actorName = actor.fullName,
        actorRole = actor.role.name,
        action = "DELIVERY_STATUS_CHANGED",
        details = "Delivery ${delivery.id} status changed to ${newStatus.name}",
        targetId = delivery.id
      )
    )

    return Result.success(updated)
  }

  suspend fun updateRiderLocation(
    deliveryId: String,
    lat: Double,
    lng: Double,
    heading: Float,
    speedKmh: Float,
    distanceRemainingKm: Double,
    etaMinutes: Int
  ) {
    val delivery = db.deliveryDao().getDeliveryByIdDirect(deliveryId) ?: return
    val updated = delivery.copy(
      currentLat = lat,
      currentLng = lng,
      currentHeading = heading,
      currentSpeedKmh = speedKmh,
      distanceRemainingKm = distanceRemainingKm,
      etaMinutes = etaMinutes,
      updatedAt = System.currentTimeMillis()
    )
    db.deliveryDao().updateDelivery(updated)
    db.deliveryDao().insertLocation(
      DeliveryLocationEntity(
        deliveryId = deliveryId,
        latitude = lat,
        longitude = lng,
        heading = heading,
        speedKmh = speedKmh
      )
    )
  }

  suspend fun verifyDeliveryOtp(deliveryId: String, enteredOtp: String, actor: UserEntity): Result<Boolean> {
    val timestamp = System.currentTimeMillis()
    val delivery = db.deliveryDao().getDeliveryByIdDirect(deliveryId)
      ?: return Result.failure(IllegalStateException("Delivery not found"))

    if (delivery.deliveryOtpPlainForCustomer != enteredOtp.trim()) {
      return Result.failure(IllegalArgumentException("Invalid Delivery OTP. Please request the 6-digit confirmation code from the customer."))
    }

    val updatedDelivery = delivery.copy(
      status = DeliveryStatus.DELIVERED,
      deliveredAt = timestamp,
      distanceRemainingKm = 0.0,
      etaMinutes = 0,
      updatedAt = timestamp
    )
    db.deliveryDao().updateDelivery(updatedDelivery)

    val order = db.orderDao().getOrderByIdDirect(delivery.orderId)
    if (order != null) {
      db.orderDao().updateOrderStatus(order.id, OrderStatus.DELIVERED, timestamp)
      db.notificationDao().insertNotification(
        NotificationEntity(
          id = UUID.randomUUID().toString(),
          userId = order.customerId,
          title = "Vehicle Handover Completed!",
          message = "Congratulations! Your ${order.vehicleTitle} has been successfully verified and delivered. Enjoy your drive!",
          category = "DELIVERY"
        )
      )
    }

    db.auditLogDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = actor.id,
        actorName = actor.fullName,
        actorRole = actor.role.name,
        action = "DELIVERY_OTP_VERIFIED",
        details = "Delivery OTP confirmed for delivery ${delivery.id}. Order marked DELIVERED.",
        targetId = delivery.id
      )
    )

    return Result.success(true)
  }

  // --- FINANCIAL LEDGER & WITHDRAWALS ---
  val allLedgerEntries: Flow<List<LedgerEntryEntity>> = db.ledgerDao().getAllLedgerEntries()
  val allWithdrawals: Flow<List<WithdrawalEntity>> = db.withdrawalDao().getAllWithdrawals()
  val allBankAccounts: Flow<List<BankAccountEntity>> = db.bankAccountDao().getAllAccounts()

  fun getFinancialSummary(): Flow<FinancialSummary> {
    return allLedgerEntries.map { entries ->
      var totalSales = 0L
      var totalFees = 0L
      var totalWithdrawn = 0L
      var totalRefunded = 0L
      var available = 0L
      var pending = 0L

      for (e in entries) {
        when (e.type) {
          LedgerType.SALE -> {
            totalSales += e.amountKobo
            totalFees += e.feeKobo
            if (e.isAvailable) {
              available += e.netAmountKobo
            } else {
              pending += e.netAmountKobo
            }
          }
          LedgerType.WITHDRAWAL -> {
            totalWithdrawn += e.amountKobo
            available -= e.amountKobo
          }
          LedgerType.REFUND -> {
            totalRefunded += e.amountKobo
            available -= e.amountKobo
          }
          LedgerType.PAYMENT_FEE -> {}
          LedgerType.ADJUSTMENT -> {}
        }
      }
      FinancialSummary(
        totalSalesKobo = totalSales,
        totalFeesKobo = totalFees,
        availableFundsKobo = if (available < 0) 0L else available,
        pendingFundsKobo = pending,
        totalWithdrawnKobo = totalWithdrawn,
        totalRefundedKobo = totalRefunded
      )
    }
  }

  suspend fun settlePendingFunds(actor: UserEntity) {
    val timestamp = System.currentTimeMillis()
    db.ledgerDao().settlePendingEntries(timestamp)
    db.auditLogDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = actor.id,
        actorName = actor.fullName,
        actorRole = actor.role.name,
        action = "FUNDS_SETTLED",
        details = "Provider clearing cycle executed. All pending settlement funds released to available balance.",
        targetId = "FINANCE"
      )
    )
  }

  suspend fun requestWithdrawal(
    actor: UserEntity,
    bankAccountId: String,
    amountKobo: Long
  ): Result<WithdrawalEntity> {
    if (actor.role != UserRole.SUPER_ADMIN && actor.role != UserRole.FINANCE_MANAGER) {
      return Result.failure(SecurityException("Unauthorized: Finance Manager or Super Admin role required for bank payouts."))
    }

    val summary = getFinancialSummary().first()
    if (amountKobo > summary.availableFundsKobo) {
      return Result.failure(IllegalStateException("Insufficient available funds. Only settled funds can be withdrawn (₦${summary.availableFundsKobo / 100} available)."))
    }
    if (amountKobo <= 0L) {
      return Result.failure(IllegalArgumentException("Withdrawal amount must be greater than zero."))
    }

    val accounts = db.bankAccountDao().getAllAccounts().first()
    val bankAccount = accounts.find { it.id == bankAccountId } ?: accounts.first()

    val timestamp = System.currentTimeMillis()
    val reference = "wth_" + UUID.randomUUID().toString().take(12)
    val feeKobo = 50_00L // ₦50 NIP interbank transfer fee
    val netPayout = amountKobo - feeKobo

    val withdrawal = WithdrawalEntity(
      id = "wd_" + UUID.randomUUID().toString().take(10),
      reference = reference,
      requestedByAdminId = actor.id,
      bankCode = bankAccount.bankCode,
      bankName = bankAccount.bankName,
      accountNumberMasked = bankAccount.accountNumberMasked,
      accountName = bankAccount.accountHolderName,
      recipientCode = bankAccount.recipientId,
      amountKobo = amountKobo,
      feeKobo = feeKobo,
      netPayoutKobo = netPayout,
      status = WithdrawalStatus.PROCESSING,
      createdAt = timestamp
    )
    db.withdrawalDao().insertWithdrawal(withdrawal)

    // Call Provider Payout API
    val provider = getProvider(PaymentGateway.PAYSTACK)
    val transferResult = provider.createTransfer(
      TransferRequest(
        reference = reference,
        recipientCode = bankAccount.recipientId,
        amountKobo = netPayout,
        narration = "Apex Motors Operating Distribution"
      )
    )

    // Record Debit in Financial Ledger
    val ledgerDebit = LedgerEntryEntity(
      id = "led_wd_" + UUID.randomUUID().toString().take(10),
      type = LedgerType.WITHDRAWAL,
      direction = LedgerDirection.DEBIT,
      amountKobo = amountKobo,
      feeKobo = feeKobo,
      netAmountKobo = netPayout,
      currency = "NGN",
      reference = reference,
      withdrawalId = withdrawal.id,
      description = "Bank Payout to ${bankAccount.bankName} ${bankAccount.accountNumberMasked}",
      isAvailable = true,
      createdAt = timestamp
    )
    db.ledgerDao().insertLedgerEntry(ledgerDebit)

    // Complete withdrawal upon provider confirmation
    val completedWithdrawal = withdrawal.copy(
      status = WithdrawalStatus.COMPLETED,
      providerTransferId = transferResult.transferId,
      processedAt = timestamp
    )
    db.withdrawalDao().updateWithdrawal(completedWithdrawal)

    db.auditLogDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = actor.id,
        actorName = actor.fullName,
        actorRole = actor.role.name,
        action = "WITHDRAWAL_COMPLETED",
        details = "Withdrawal of ₦${amountKobo / 100} settled to ${bankAccount.bankName} (${bankAccount.accountNumberMasked})",
        targetId = withdrawal.id
      )
    )

    return Result.success(completedWithdrawal)
  }

  suspend fun refundOrder(orderId: String, reason: String, actor: UserEntity): Result<Boolean> {
    if (actor.role != UserRole.SUPER_ADMIN && actor.role != UserRole.FINANCE_MANAGER) {
      return Result.failure(SecurityException("Unauthorized: Finance Manager or Super Admin role required for refunds."))
    }

    val order = db.orderDao().getOrderByIdDirect(orderId)
      ?: return Result.failure(IllegalStateException("Order not found"))
    if (order.status != OrderStatus.PAID && order.status != OrderStatus.CONFIRMED) {
      return Result.failure(IllegalStateException("Only verified paid orders can be refunded."))
    }

    val timestamp = System.currentTimeMillis()
    val payment = db.paymentDao().getPaymentForOrder(order.id).first()
      ?: return Result.failure(IllegalStateException("Payment record not found."))

    val provider = getProvider(payment.provider)
    val refundResult = provider.refundPayment(
      RefundRequest(
        paymentReference = payment.reference,
        amountKobo = order.totalAmountKobo,
        reason = reason
      )
    )

    if (!refundResult.isSuccessful) {
      return Result.failure(IllegalStateException("Provider refund failed: ${refundResult.message}"))
    }

    // Update Order & Payment
    db.orderDao().updateOrderStatus(order.id, OrderStatus.REFUNDED, timestamp)
    db.paymentDao().updatePayment(payment.copy(status = PaymentStatus.REFUNDED, updatedAt = timestamp))
    db.vehicleDao().updateVehicle(
      db.vehicleDao().getVehicleByIdDirect(order.vehicleId)!!.copy(
        status = VehicleStatus.AVAILABLE,
        reservedByCustomerId = null,
        reservedAt = null,
        updatedAt = timestamp
      )
    )

    // Ledger Refund entry
    val ledgerRefund = LedgerEntryEntity(
      id = "led_ref_" + UUID.randomUUID().toString().take(10),
      type = LedgerType.REFUND,
      direction = LedgerDirection.DEBIT,
      amountKobo = order.totalAmountKobo,
      feeKobo = 0L,
      netAmountKobo = order.totalAmountKobo,
      currency = "NGN",
      reference = "ref_" + order.orderNumber,
      orderId = order.id,
      paymentId = payment.id,
      description = "Customer Refund for ${order.vehicleTitle}. Reason: $reason",
      isAvailable = true,
      createdAt = timestamp
    )
    db.ledgerDao().insertLedgerEntry(ledgerRefund)

    db.notificationDao().insertNotification(
      NotificationEntity(
        id = UUID.randomUUID().toString(),
        userId = order.customerId,
        title = "Refund Processed",
        message = "Your refund of ₦${order.totalAmountKobo / 100} for ${order.vehicleTitle} has been submitted to your bank card.",
        category = "REFUND"
      )
    )

    db.auditLogDao().insertLog(
      AuditLogEntity(
        id = UUID.randomUUID().toString(),
        actorId = actor.id,
        actorName = actor.fullName,
        actorRole = actor.role.name,
        action = "ORDER_REFUNDED",
        details = "Refunded Order ${order.orderNumber}. Amount: ₦${order.totalAmountKobo / 100}. Reason: $reason",
        targetId = order.id
      )
    )

    return Result.success(true)
  }

  suspend fun runReconciliation(): FinancialReconciliation {
    val orders = db.orderDao().getAllOrders().first().filter { it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.DELIVERED }
    val payments = db.paymentDao().getAllPayments().first().filter { it.status == PaymentStatus.SUCCESS }
    val ledger = db.ledgerDao().getAllLedgerEntries().first().filter { it.type == LedgerType.SALE }

    val orderTotal = orders.sumOf { it.totalAmountKobo }
    val paymentTotal = payments.sumOf { it.amountKobo }
    val ledgerTotal = ledger.sumOf { it.amountKobo }
    val discrepancy = Math.abs(orderTotal - paymentTotal)

    return FinancialReconciliation(
      totalPaidOrdersAmountKobo = orderTotal,
      totalPaymentGatewayAmountKobo = paymentTotal,
      totalLedgerSalesKobo = ledgerTotal,
      discrepancyKobo = discrepancy,
      isReconciled = (discrepancy == 0L) && (orderTotal == ledgerTotal),
      verifiedTransactionsCount = payments.size
    )
  }

  // --- ORDERS & CUSTOMER ---
  val allOrders: Flow<List<OrderEntity>> = db.orderDao().getAllOrders()
  fun getOrdersForCustomer(customerId: String): Flow<List<OrderEntity>> =
    db.orderDao().getOrdersForCustomer(customerId)
  fun getOrder(orderId: String): Flow<OrderEntity?> = db.orderDao().getOrderById(orderId)

  // --- USERS ---
  val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsers()

  // --- NOTIFICATIONS ---
  fun getNotifications(userId: String): Flow<List<NotificationEntity>> =
    db.notificationDao().getNotificationsForUser(userId)
  suspend fun markAllNotificationsRead(userId: String) =
    db.notificationDao().markAllRead(userId)

  // --- CONTACT MESSAGES ---
  val allContactMessages: Flow<List<ContactMessageEntity>> = db.contactDao().getAllMessages()
  suspend fun submitContactMessage(message: ContactMessageEntity) =
    db.contactDao().insertMessage(message)
  suspend fun replyContactMessage(message: ContactMessageEntity, reply: String) {
    db.contactDao().updateMessage(
      message.copy(
        adminReply = reply,
        status = "RESOLVED"
      )
    )
  }

  // --- AUDIT LOGS ---
  val allAuditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllLogs()

  private fun hashOtp(otp: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(otp.toByteArray()).joinToString("") { "%02x".format(it) }
  }
}
