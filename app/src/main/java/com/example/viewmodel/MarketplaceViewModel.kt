package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.BankAccountEntity
import com.example.data.entity.ContactMessageEntity
import com.example.data.entity.DeliveryEntity
import com.example.data.entity.LedgerEntryEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.VehicleEntity
import com.example.data.entity.WithdrawalEntity
import com.example.data.model.BodyType
import com.example.data.model.DeliveryStatus
import com.example.data.model.FuelType
import com.example.data.model.PaymentGateway
import com.example.data.model.TransmissionType
import com.example.data.model.UserRole
import com.example.data.model.VehicleCondition
import com.example.data.model.VehicleStatus
import com.example.payment.FlutterwavePaymentProvider
import com.example.payment.PaystackPaymentProvider
import com.example.repository.FinancialReconciliation
import com.example.repository.FinancialSummary
import com.example.repository.MarketplaceRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
  FEATURED("Featured & Prestige"),
  PRICE_LOW_TO_HIGH("Price: Low to High"),
  PRICE_HIGH_TO_LOW("Price: High to Low"),
  NEWEST("Newest Model Year"),
  MILEAGE("Lowest Mileage")
}

sealed class CheckoutState {
  object Idle : CheckoutState()
  object Loading : CheckoutState()
  data class HostedCheckoutReady(
    val orderId: String,
    val reference: String,
    val checkoutUrl: String,
    val gateway: PaymentGateway
  ) : CheckoutState()
  data class Success(val orderId: String, val reference: String) : CheckoutState()
  data class Error(val message: String) : CheckoutState()
}

data class VehicleFilterCriteria(
  val query: String = "",
  val brand: String? = null,
  val bodyType: BodyType? = null,
  val transmission: TransmissionType? = null,
  val fuelType: FuelType? = null,
  val condition: VehicleCondition? = null,
  val status: VehicleStatus? = null,
  val sort: SortOption = SortOption.FEATURED
)

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application, viewModelScope)
  private val paystack = PaystackPaymentProvider()
  private val flutterwave = FlutterwavePaymentProvider()
  val repository = MarketplaceRepository(database, paystack, flutterwave)

  // Current logged in user (switchable to test all roles)
  private val _currentUser = MutableStateFlow<UserEntity>(
    UserEntity(
      id = "user_cust_1",
      fullName = "Adebayo Williams",
      email = "adebayo@example.com",
      phone = "+234 803 555 0192",
      role = UserRole.CUSTOMER,
      address = "Bourdillon Rd, Ikoyi, Lagos"
    )
  )
  val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

  val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Vehicles
  val allVehicles: StateFlow<List<VehicleEntity>> = repository.allVehicles
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Filters & Search State
  val filterCriteria = MutableStateFlow(VehicleFilterCriteria())

  val filteredVehicles: StateFlow<List<VehicleEntity>> = combine(
    allVehicles,
    filterCriteria
  ) { list, criteria ->
    var result = list

    if (criteria.query.isNotBlank()) {
      val q = criteria.query.trim().lowercase()
      result = result.filter {
        it.brand.lowercase().contains(q) ||
          it.model.lowercase().contains(q) ||
          it.year.toString().contains(q) ||
          it.description.lowercase().contains(q) ||
          it.location.lowercase().contains(q) ||
          it.vin.lowercase().contains(q)
      }
    }

    if (criteria.brand != null) {
      result = result.filter { it.brand.equals(criteria.brand, ignoreCase = true) }
    }
    if (criteria.bodyType != null) {
      result = result.filter { it.bodyType == criteria.bodyType }
    }
    if (criteria.transmission != null) {
      result = result.filter { it.transmission == criteria.transmission }
    }
    if (criteria.fuelType != null) {
      result = result.filter { it.fuelType == criteria.fuelType }
    }
    if (criteria.condition != null) {
      result = result.filter { it.condition == criteria.condition }
    }
    if (criteria.status != null) {
      result = result.filter { it.status == criteria.status }
    }

    when (criteria.sort) {
      SortOption.FEATURED -> result.sortedWith(compareByDescending<VehicleEntity> { it.featured }.thenByDescending { it.createdAt })
      SortOption.PRICE_LOW_TO_HIGH -> result.sortedBy { it.priceKobo }
      SortOption.PRICE_HIGH_TO_LOW -> result.sortedByDescending { it.priceKobo }
      SortOption.NEWEST -> result.sortedByDescending { it.year }
      SortOption.MILEAGE -> result.sortedBy { it.mileageKm }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Saved Vehicles
  val savedVehicles: StateFlow<List<VehicleEntity>> = _currentUser.flatMapLatest { user ->
    repository.getSavedVehicles(user.id)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Selected vehicle for details
  val selectedVehicle = MutableStateFlow<VehicleEntity?>(null)

  // Orders
  val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val customerOrders: StateFlow<List<OrderEntity>> = _currentUser.flatMapLatest { user ->
    repository.getOrdersForCustomer(user.id)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Selected Order for Tracking
  val trackingOrder = MutableStateFlow<OrderEntity?>(null)
  val trackingDelivery: StateFlow<DeliveryEntity?> = trackingOrder.flatMapLatest { order ->
    if (order != null) repository.getDeliveryForOrder(order.id) else MutableStateFlow(null)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Deliveries for Rider & Admin
  val allDeliveries: StateFlow<List<DeliveryEntity>> = repository.allDeliveries
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val riderDeliveries: StateFlow<List<DeliveryEntity>> = _currentUser.flatMapLatest { user ->
    repository.getDeliveriesForRider(user.id)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Finance & Ledger
  val allLedgerEntries: StateFlow<List<LedgerEntryEntity>> = repository.allLedgerEntries
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.allWithdrawals
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val bankAccounts: StateFlow<List<BankAccountEntity>> = repository.allBankAccounts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val financialSummary: StateFlow<FinancialSummary> = repository.getFinancialSummary()
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      FinancialSummary(0, 0, 0, 0, 0, 0)
    )

  val reconciliationResult = MutableStateFlow<FinancialReconciliation?>(null)

  // Notifications
  val notifications: StateFlow<List<NotificationEntity>> = _currentUser.flatMapLatest { user ->
    repository.getNotifications(user.id)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Contact Messages & Audit Logs
  val contactMessages: StateFlow<List<ContactMessageEntity>> = repository.allContactMessages
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Checkout State
  val checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)

  // Feedback messages / Snackbar
  private val _snackbarMessage = MutableSharedFlow<String>()
  val snackbarMessage = _snackbarMessage.asSharedFlow()

  fun showMessage(msg: String) {
    viewModelScope.launch { _snackbarMessage.emit(msg) }
  }

  fun switchUser(user: UserEntity) {
    _currentUser.value = user
    showMessage("Switched persona to: ${user.fullName} (${user.role.label})")
  }

  fun selectVehicle(vehicle: VehicleEntity?) {
    selectedVehicle.value = vehicle
  }

  fun toggleSaveVehicle(vehicleId: String) {
    viewModelScope.launch {
      repository.toggleSaveVehicle(_currentUser.value.id, vehicleId)
    }
  }

  fun clearFilters() {
    filterCriteria.value = VehicleFilterCriteria()
  }

  fun updateFilterCriteria(transform: (VehicleFilterCriteria) -> VehicleFilterCriteria) {
    filterCriteria.value = transform(filterCriteria.value)
  }

  // --- CHECKOUT & PAYMENT FLOW ---
  fun startCheckout(
    vehicle: VehicleEntity,
    deliveryAddress: String,
    deliveryCity: String,
    deliveryState: String,
    contactPhone: String,
    gateway: PaymentGateway
  ) {
    viewModelScope.launch {
      checkoutState.value = CheckoutState.Loading
      val result = repository.initiateVehiclePurchase(
        vehicleId = vehicle.id,
        customer = _currentUser.value,
        deliveryAddress = deliveryAddress,
        deliveryCity = deliveryCity,
        deliveryState = deliveryState,
        contactPhone = contactPhone,
        gateway = gateway
      )
      result.fold(
        onSuccess = { (orderId, reference, checkoutUrl) ->
          checkoutState.value = CheckoutState.HostedCheckoutReady(
            orderId = orderId,
            reference = reference,
            checkoutUrl = checkoutUrl,
            gateway = gateway
          )
        },
        onFailure = { error ->
          checkoutState.value = CheckoutState.Error(error.message ?: "Failed to initiate checkout")
          showMessage("Error: ${error.message}")
        }
      )
    }
  }

  fun completeHostedPayment(reference: String, gateway: PaymentGateway) {
    viewModelScope.launch {
      checkoutState.value = CheckoutState.Loading
      val result = repository.verifyAndCompletePayment(reference, gateway)
      result.fold(
        onSuccess = { order ->
          checkoutState.value = CheckoutState.Success(order.id, reference)
          trackingOrder.value = order
          showMessage("Payment Verified! Flatbed dispatch carrier assigned.")
        },
        onFailure = { error ->
          checkoutState.value = CheckoutState.Error(error.message ?: "Verification failed")
          showMessage("Payment verification failed: ${error.message}")
        }
      )
    }
  }

  fun resetCheckout() {
    checkoutState.value = CheckoutState.Idle
  }

  // --- DISPATCH & RIDER OPERATIONS ---
  fun updateDeliveryStatus(deliveryId: String, status: DeliveryStatus) {
    viewModelScope.launch {
      val result = repository.updateDeliveryStatus(deliveryId, status, _currentUser.value)
      result.fold(
        onSuccess = { showMessage("Carrier updated: ${status.label}") },
        onFailure = { showMessage("Update failed: ${it.message}") }
      )
    }
  }

  fun simulateGpsMovement(delivery: DeliveryEntity) {
    viewModelScope.launch {
      // Advance carrier coordinates closer to customer destination
      val newLat = delivery.currentLat + 0.0042
      val newLng = delivery.currentLng + 0.0035
      val newSpeed = (45..75).random().toFloat()
      val newDistance = (delivery.distanceRemainingKm - 2.1).coerceAtLeast(0.5)
      val newEta = (delivery.etaMinutes - 6).coerceAtLeast(5)

      repository.updateRiderLocation(
        deliveryId = delivery.id,
        lat = newLat,
        lng = newLng,
        heading = 62f,
        speedKmh = newSpeed,
        distanceRemainingKm = newDistance,
        etaMinutes = newEta
      )
      showMessage("Live GPS ping broadcast: ${String.format("%.4f", newLat)}, ${String.format("%.4f", newLng)} (${newSpeed.toInt()} km/h)")
    }
  }

  fun verifyDeliveryOtp(deliveryId: String, enteredOtp: String) {
    viewModelScope.launch {
      val result = repository.verifyDeliveryOtp(deliveryId, enteredOtp, _currentUser.value)
      result.fold(
        onSuccess = { showMessage("Vehicle Handover Confirmed! OTP Validated.") },
        onFailure = { showMessage("OTP Error: ${it.message}") }
      )
    }
  }

  // --- FINANCE OPERATIONS ---
  fun requestWithdrawal(bankAccountId: String, amountKobo: Long) {
    viewModelScope.launch {
      val result = repository.requestWithdrawal(_currentUser.value, bankAccountId, amountKobo)
      result.fold(
        onSuccess = { showMessage("Withdrawal of ₦${amountKobo / 100} initiated via Paystack Payout") },
        onFailure = { showMessage("Withdrawal failed: ${it.message}") }
      )
    }
  }

  fun settlePendingFunds() {
    viewModelScope.launch {
      repository.settlePendingFunds(_currentUser.value)
      showMessage("Provider settlement completed. Pending funds transferred to Available Balance.")
    }
  }

  fun processRefund(orderId: String, reason: String) {
    viewModelScope.launch {
      val result = repository.refundOrder(orderId, reason, _currentUser.value)
      result.fold(
        onSuccess = { showMessage("Order refunded successfully. Vehicle restored to Available inventory.") },
        onFailure = { showMessage("Refund failed: ${it.message}") }
      )
    }
  }

  fun runReconciliation() {
    viewModelScope.launch {
      val rec = repository.runReconciliation()
      reconciliationResult.value = rec
      showMessage(if (rec.isReconciled) "Financial Audit Passed: 100% Reconciled!" else "Discrepancy detected: ₦${rec.discrepancyKobo / 100}")
    }
  }

  // --- VEHICLE INVENTORY MANAGEMENT (ADMIN) ---
  fun saveVehicle(vehicle: VehicleEntity, isNew: Boolean) {
    viewModelScope.launch {
      if (isNew) {
        repository.insertVehicle(vehicle, _currentUser.value)
        showMessage("Vehicle ${vehicle.brand} ${vehicle.model} added to inventory.")
      } else {
        repository.updateVehicle(vehicle, _currentUser.value)
        showMessage("Vehicle ${vehicle.brand} ${vehicle.model} updated.")
      }
    }
  }

  fun deleteVehicle(vehicle: VehicleEntity) {
    viewModelScope.launch {
      repository.deleteVehicle(vehicle, _currentUser.value)
      showMessage("Vehicle ${vehicle.brand} ${vehicle.model} deleted from vault.")
    }
  }

  // --- CONTACT & SUPPORT ---
  fun submitContact(name: String, email: String, phone: String, subject: String, category: String, message: String) {
    viewModelScope.launch {
      val msg = ContactMessageEntity(
        id = "msg_" + UUID.randomUUID().toString().take(10),
        name = name,
        email = email,
        phone = phone,
        subject = subject,
        category = category,
        message = message
      )
      repository.submitContactMessage(msg)
      showMessage("Your inquiry has been submitted to our VIP Client Concierge.")
    }
  }

  fun replyContact(msg: ContactMessageEntity, reply: String) {
    viewModelScope.launch {
      repository.replyContactMessage(msg, reply)
      showMessage("Staff reply sent to client ${msg.name}.")
    }
  }

  // AI Assistance Integration
  private val aiService = com.example.ai.CharlesAiService(database)

  private val _customerAiMessages = MutableStateFlow<List<com.example.ai.ChatMessage>>(
    listOf(
      com.example.ai.ChatMessage(
        sender = com.example.ai.MessageSender.ASSISTANT,
        text = "Hello! I am Charles Assistant, your private luxury automotive concierge. I can search our certified vehicle vault, explain technical specifications, help with escrow checkout, or track your live carrier delivery. How may I assist your acquisition today?"
      )
    )
  )
  val customerAiMessages: StateFlow<List<com.example.ai.ChatMessage>> = _customerAiMessages.asStateFlow()

  private val _adminAiMessages = MutableStateFlow<List<com.example.ai.ChatMessage>>(
    listOf(
      com.example.ai.ChatMessage(
        sender = com.example.ai.MessageSender.ASSISTANT,
        text = "Charles Admin AI Operations Intelligence online. Authenticated for role-based operational telemetry queries. How can I assist management today?"
      )
    )
  )
  val adminAiMessages: StateFlow<List<com.example.ai.ChatMessage>> = _adminAiMessages.asStateFlow()

  val isAiThinking = MutableStateFlow(false)

  // Admin Authentication & Access Security
  val adminSessionActive = MutableStateFlow(false)
  private val adminPasswordHash = MutableStateFlow(com.example.data.security.SecurityUtils.hashPassword("CharlesAdmin@2025"))

  fun authenticateAdmin(password: String): Result<Boolean> {
    val actor = _currentUser.value
    if (!com.example.data.security.SecurityUtils.isAdministrativeRole(actor.role)) {
      viewModelScope.launch {
        repository.allAuditLogs
        database.auditLogDao().insertLog(
          AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorId = actor.id,
            actorName = actor.fullName,
            actorRole = actor.role.name,
            action = "UNAUTHORIZED_ADMIN_ACCESS_ATTEMPT",
            details = "User with role '${actor.role.label}' attempted to access /admin portal. Rejected.",
            targetId = "/admin"
          )
        )
      }
      return Result.failure(SecurityException("Access Denied: User account '${actor.fullName}' does not hold administrative clearance."))
    }

    val isValid = com.example.data.security.SecurityUtils.verifyPassword(password, adminPasswordHash.value)
    if (!isValid) {
      viewModelScope.launch {
        database.auditLogDao().insertLog(
          AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorId = actor.id,
            actorName = actor.fullName,
            actorRole = actor.role.name,
            action = "ADMIN_AUTHENTICATION_FAILED",
            details = "Incorrect password submitted for administrative portal.",
            targetId = "/admin"
          )
        )
      }
      return Result.failure(IllegalArgumentException("Invalid administrator credentials."))
    }

    adminSessionActive.value = true
    viewModelScope.launch {
      database.auditLogDao().insertLog(
        AuditLogEntity(
          id = UUID.randomUUID().toString(),
          actorId = actor.id,
          actorName = actor.fullName,
          actorRole = actor.role.name,
          action = "ADMIN_AUTHENTICATION_SUCCESS",
          details = "Administrator authenticated into /admin console.",
          targetId = "/admin"
        )
      )
    }
    return Result.success(true)
  }

  fun logoutAdmin() {
    adminSessionActive.value = false
    showMessage("Administrator session terminated.")
  }

  fun changeAdminPassword(oldPass: String, newPass: String): Result<Boolean> {
    if (!com.example.data.security.SecurityUtils.verifyPassword(oldPass, adminPasswordHash.value)) {
      return Result.failure(IllegalArgumentException("Current password incorrect."))
    }
    if (newPass.length < 8) {
      return Result.failure(IllegalArgumentException("New password must be at least 8 characters."))
    }
    adminPasswordHash.value = com.example.data.security.SecurityUtils.hashPassword(newPass)
    showMessage("Administrator password updated securely.")
    return Result.success(true)
  }

  fun sendCustomerAiMessage(query: String) {
    if (query.isBlank()) return
    val userMsg = com.example.ai.ChatMessage(sender = com.example.ai.MessageSender.USER, text = query)
    _customerAiMessages.value = _customerAiMessages.value + userMsg

    viewModelScope.launch {
      isAiThinking.value = true
      try {
        val reply = aiService.processCustomerMessage(query, _currentUser.value)
        _customerAiMessages.value = _customerAiMessages.value + reply
      } catch (e: Exception) {
        _customerAiMessages.value = _customerAiMessages.value + com.example.ai.ChatMessage(
          sender = com.example.ai.MessageSender.ASSISTANT,
          text = "I apologize, our concierge database experienced a temporary delay. Please feel free to re-ask or reach out to our VIP hotline."
        )
      } finally {
        isAiThinking.value = false
      }
    }
  }

  fun sendAdminAiMessage(query: String) {
    if (query.isBlank()) return
    val userMsg = com.example.ai.ChatMessage(sender = com.example.ai.MessageSender.USER, text = query)
    _adminAiMessages.value = _adminAiMessages.value + userMsg

    viewModelScope.launch {
      isAiThinking.value = true
      try {
        val reply = aiService.processAdminMessage(query, _currentUser.value)
        _adminAiMessages.value = _adminAiMessages.value + reply
      } catch (e: Exception) {
        _adminAiMessages.value = _adminAiMessages.value + com.example.ai.ChatMessage(
          sender = com.example.ai.MessageSender.ASSISTANT,
          text = "Error processing operational query: ${e.message}"
        )
      } finally {
        isAiThinking.value = false
      }
    }
  }

  fun markNotificationsRead() {
    viewModelScope.launch {
      repository.markAllNotificationsRead(_currentUser.value.id)
    }
  }

  fun recordAuditLog(action: String, details: String, targetId: String? = null) {
    viewModelScope.launch {
      val actor = _currentUser.value
      database.auditLogDao().insertLog(
        AuditLogEntity(
          id = UUID.randomUUID().toString(),
          actorId = actor.id,
          actorName = actor.fullName,
          actorRole = actor.role.name,
          action = action,
          details = details,
          targetId = targetId
        )
      )
    }
  }
}
