package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.BodyType
import com.example.data.model.DeliveryStatus
import com.example.data.model.FuelType
import com.example.data.model.LedgerDirection
import com.example.data.model.LedgerType
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentStatus
import com.example.data.model.TransmissionType
import com.example.data.model.UserRole
import com.example.data.model.VehicleCondition
import com.example.data.model.VehicleStatus
import com.example.data.model.WithdrawalStatus

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
  @PrimaryKey val id: String,
  val fullName: String,
  val email: String,
  val phone: String,
  val role: UserRole,
  val avatarUrl: String = "",
  val address: String = "Victoria Island, Lagos",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vehicles", indices = [Index(value = ["vin"], unique = true)])
data class VehicleEntity(
  @PrimaryKey val id: String,
  val brand: String,
  val model: String,
  val year: Int,
  val priceKobo: Long, // Integer monetary unit: no floating-point arithmetic
  val currency: String = "NGN",
  val mileageKm: Int,
  val transmission: TransmissionType,
  val fuelType: FuelType,
  val bodyType: BodyType,
  val color: String,
  val condition: VehicleCondition,
  val description: String,
  val location: String, // e.g. "Lekki Phase 1 Showroom, Lagos"
  val status: VehicleStatus = VehicleStatus.AVAILABLE,
  val featured: Boolean = false,
  val vin: String,
  val engineSpecs: String,
  val horsepower: Int,
  val acceleration0to100: Float,
  val imageResName: String, // drawable resource name (e.g. "img_hero_car", "img_suv_luxury")
  val reservedByCustomerId: String? = null,
  val reservedAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_vehicles", primaryKeys = ["userId", "vehicleId"])
data class SavedVehicleEntity(
  val userId: String,
  val vehicleId: String,
  val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders", indices = [Index(value = ["orderNumber"], unique = true)])
data class OrderEntity(
  @PrimaryKey val id: String,
  val orderNumber: String,
  val customerId: String,
  val vehicleId: String,
  val vehicleTitle: String,
  val vehicleVin: String,
  val basePriceKobo: Long, // Authoritative price from database
  val deliveryFeeKobo: Long,
  val documentationFeeKobo: Long,
  val totalAmountKobo: Long,
  val currency: String = "NGN",
  val status: OrderStatus = OrderStatus.PENDING,
  val deliveryAddress: String,
  val deliveryCity: String,
  val deliveryState: String,
  val contactPhone: String,
  val paymentReference: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments", indices = [Index(value = ["reference"], unique = true)])
data class PaymentEntity(
  @PrimaryKey val id: String,
  val orderId: String,
  val provider: PaymentGateway,
  val reference: String,
  val amountKobo: Long,
  val currency: String = "NGN",
  val status: PaymentStatus = PaymentStatus.INITIATED,
  val providerTransactionId: String? = null,
  val hostedCheckoutUrl: String = "",
  val customerEmail: String,
  val paidAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "webhook_events", indices = [Index(value = ["provider", "eventId"], unique = true)])
data class WebhookEventEntity(
  @PrimaryKey val id: String,
  val provider: String,
  val eventId: String,
  val eventType: String,
  val processed: Boolean = false,
  val payloadJson: String,
  val receivedAt: Long = System.currentTimeMillis(),
  val processedAt: Long? = null
)

@Entity(tableName = "ledger_entries", indices = [Index(value = ["reference"])])
data class LedgerEntryEntity(
  @PrimaryKey val id: String,
  val type: LedgerType,
  val direction: LedgerDirection,
  val amountKobo: Long,
  val feeKobo: Long = 0L,
  val netAmountKobo: Long,
  val currency: String = "NGN",
  val reference: String,
  val orderId: String? = null,
  val paymentId: String? = null,
  val withdrawalId: String? = null,
  val description: String,
  val isAvailable: Boolean = false, // Segregates PENDING vs AVAILABLE funds
  val settlementAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals", indices = [Index(value = ["reference"], unique = true)])
data class WithdrawalEntity(
  @PrimaryKey val id: String,
  val reference: String,
  val requestedByAdminId: String,
  val bankCode: String,
  val bankName: String,
  val accountNumberMasked: String, // Masked (e.g. ******4819)
  val accountName: String,
  val recipientCode: String,
  val amountKobo: Long,
  val feeKobo: Long,
  val netPayoutKobo: Long,
  val status: WithdrawalStatus = WithdrawalStatus.PENDING,
  val providerTransferId: String? = null,
  val processedAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
  @PrimaryKey val id: String,
  val bankName: String,
  val bankCode: String,
  val accountNumberMasked: String,
  val accountHolderName: String,
  val recipientId: String,
  val isDefault: Boolean = false
)

@Entity(tableName = "deliveries", indices = [Index(value = ["orderId"], unique = true)])
data class DeliveryEntity(
  @PrimaryKey val id: String,
  val orderId: String,
  val vehicleId: String,
  val riderId: String? = null,
  val riderName: String? = null,
  val riderPhone: String? = null,
  val carrierVehicle: String? = "Mercedes-Benz Actros 3340 Flatbed",
  val carrierPlate: String? = "APP-402-XA",
  val status: DeliveryStatus = DeliveryStatus.ASSIGNED,
  val pickupHubAddress: String = "Apex Central Showroom & Logistics Hub, Lekki Expressway, Lagos",
  val destinationAddress: String,
  val deliveryOtpHash: String, // Secure hashed OTP
  val deliveryOtpPlainForCustomer: String, // Revealed exclusively in customer tracking view
  val currentLat: Double = 6.4281, // Lagos Lekki start
  val currentLng: Double = 3.4219,
  val currentHeading: Float = 45f,
  val currentSpeedKmh: Float = 0f,
  val distanceRemainingKm: Double = 14.2,
  val etaMinutes: Int = 35,
  val startedAt: Long? = null,
  val pickedUpAt: Long? = null,
  val deliveredAt: Long? = null,
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_locations")
data class DeliveryLocationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val deliveryId: String,
  val latitude: Double,
  val longitude: Double,
  val heading: Float,
  val speedKmh: Float,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val title: String,
  val message: String,
  val category: String, // PAYMENT, ORDER, DISPATCH, DELIVERY, FINANCE, SUPPORT
  val isRead: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contact_messages")
data class ContactMessageEntity(
  @PrimaryKey val id: String,
  val name: String,
  val email: String,
  val phone: String,
  val subject: String,
  val category: String, // Sales, Inspection, Delivery, Custom Import, Financing
  val message: String,
  val isRead: Boolean = false,
  val status: String = "NEW", // NEW, IN_PROGRESS, RESOLVED
  val adminReply: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
  @PrimaryKey val id: String,
  val actorId: String,
  val actorName: String,
  val actorRole: String,
  val action: String,
  val details: String,
  val targetId: String? = null,
  val ipAddress: String = "197.210.226.14",
  val createdAt: Long = System.currentTimeMillis()
)
