package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.example.data.model.OrderStatus
import com.example.data.model.VehicleStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
  @Query("SELECT * FROM vehicles ORDER BY featured DESC, createdAt DESC")
  fun getAllVehicles(): Flow<List<VehicleEntity>>

  @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
  fun getVehicleById(id: String): Flow<VehicleEntity?>

  @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
  suspend fun getVehicleByIdDirect(id: String): VehicleEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVehicle(vehicle: VehicleEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVehicles(vehicles: List<VehicleEntity>)

  @Update
  suspend fun updateVehicle(vehicle: VehicleEntity)

  @Delete
  suspend fun deleteVehicle(vehicle: VehicleEntity)

  @Query("UPDATE vehicles SET status = 'RESERVED', reservedByCustomerId = :customerId, reservedAt = :timestamp, updatedAt = :timestamp WHERE id = :id AND status = 'AVAILABLE'")
  suspend fun reserveVehicle(id: String, customerId: String, timestamp: Long): Int

  @Query("UPDATE vehicles SET status = 'AVAILABLE', reservedByCustomerId = NULL, reservedAt = NULL WHERE id = :id")
  suspend fun releaseReservation(id: String): Int

  @Query("UPDATE vehicles SET status = 'SOLD', updatedAt = :timestamp WHERE id = :id")
  suspend fun markSold(id: String, timestamp: Long): Int

  @Query("SELECT v.* FROM vehicles v INNER JOIN saved_vehicles s ON v.id = s.vehicleId WHERE s.userId = :userId ORDER BY s.savedAt DESC")
  fun getSavedVehiclesForUser(userId: String): Flow<List<VehicleEntity>>

  @Query("SELECT COUNT(*) > 0 FROM saved_vehicles WHERE userId = :userId AND vehicleId = :vehicleId")
  fun isVehicleSaved(userId: String, vehicleId: String): Flow<Boolean>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveVehicle(saved: SavedVehicleEntity)

  @Query("DELETE FROM saved_vehicles WHERE userId = :userId AND vehicleId = :vehicleId")
  suspend fun removeSavedVehicle(userId: String, vehicleId: String)
}

@Dao
interface OrderDao {
  @Query("SELECT * FROM orders ORDER BY createdAt DESC")
  fun getAllOrders(): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
  fun getOrdersForCustomer(customerId: String): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
  fun getOrderById(orderId: String): Flow<OrderEntity?>

  @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
  suspend fun getOrderByIdDirect(orderId: String): OrderEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: OrderEntity)

  @Update
  suspend fun updateOrder(order: OrderEntity)

  @Query("UPDATE orders SET status = :status, updatedAt = :timestamp WHERE id = :orderId")
  suspend fun updateOrderStatus(orderId: String, status: OrderStatus, timestamp: Long)
}

@Dao
interface PaymentDao {
  @Query("SELECT * FROM payments ORDER BY createdAt DESC")
  fun getAllPayments(): Flow<List<PaymentEntity>>

  @Query("SELECT * FROM payments WHERE reference = :reference LIMIT 1")
  fun getPaymentByReference(reference: String): Flow<PaymentEntity?>

  @Query("SELECT * FROM payments WHERE reference = :reference LIMIT 1")
  suspend fun getPaymentByReferenceDirect(reference: String): PaymentEntity?

  @Query("SELECT * FROM payments WHERE orderId = :orderId LIMIT 1")
  fun getPaymentForOrder(orderId: String): Flow<PaymentEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: PaymentEntity)

  @Update
  suspend fun updatePayment(payment: PaymentEntity)
}

@Dao
interface WebhookEventDao {
  @Query("SELECT * FROM webhook_events WHERE provider = :provider AND eventId = :eventId LIMIT 1")
  suspend fun getEvent(provider: String, eventId: String): WebhookEventEntity?

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertEvent(event: WebhookEventEntity): Long

  @Query("SELECT * FROM webhook_events ORDER BY receivedAt DESC")
  fun getAllEvents(): Flow<List<WebhookEventEntity>>
}

@Dao
interface LedgerDao {
  @Query("SELECT * FROM ledger_entries ORDER BY createdAt DESC")
  fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLedgerEntry(entry: LedgerEntryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLedgerEntries(entries: List<LedgerEntryEntity>)

  @Query("UPDATE ledger_entries SET isAvailable = 1, settlementAt = :timestamp WHERE isAvailable = 0")
  suspend fun settlePendingEntries(timestamp: Long)
}

@Dao
interface WithdrawalDao {
  @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
  fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

  @Query("SELECT * FROM withdrawals WHERE id = :id LIMIT 1")
  fun getWithdrawalById(id: String): Flow<WithdrawalEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

  @Update
  suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)
}

@Dao
interface DeliveryDao {
  @Query("SELECT * FROM deliveries ORDER BY updatedAt DESC")
  fun getAllDeliveries(): Flow<List<DeliveryEntity>>

  @Query("SELECT * FROM deliveries WHERE orderId = :orderId LIMIT 1")
  fun getDeliveryForOrder(orderId: String): Flow<DeliveryEntity?>

  @Query("SELECT * FROM deliveries WHERE id = :deliveryId LIMIT 1")
  fun getDeliveryById(deliveryId: String): Flow<DeliveryEntity?>

  @Query("SELECT * FROM deliveries WHERE id = :deliveryId LIMIT 1")
  suspend fun getDeliveryByIdDirect(deliveryId: String): DeliveryEntity?

  @Query("SELECT * FROM deliveries WHERE riderId = :riderId ORDER BY updatedAt DESC")
  fun getDeliveriesForRider(riderId: String): Flow<List<DeliveryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDelivery(delivery: DeliveryEntity)

  @Update
  suspend fun updateDelivery(delivery: DeliveryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLocation(location: DeliveryLocationEntity)

  @Query("SELECT * FROM delivery_locations WHERE deliveryId = :deliveryId ORDER BY timestamp ASC")
  fun getLocationsForDelivery(deliveryId: String): Flow<List<DeliveryLocationEntity>>
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = 'ALL' ORDER BY createdAt DESC")
  fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR userId = 'ALL'")
  suspend fun markAllRead(userId: String)
}

@Dao
interface ContactDao {
  @Query("SELECT * FROM contact_messages ORDER BY createdAt DESC")
  fun getAllMessages(): Flow<List<ContactMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ContactMessageEntity)

  @Update
  suspend fun updateMessage(message: ContactMessageEntity)
}

@Dao
interface AuditLogDao {
  @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC")
  fun getAllLogs(): Flow<List<AuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface UserDao {
  @Query("SELECT * FROM users ORDER BY createdAt ASC")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  fun getUserById(id: String): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  suspend fun getUserByIdDirect(id: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserEntity>)
}

@Dao
interface BankAccountDao {
  @Query("SELECT * FROM bank_accounts")
  fun getAllAccounts(): Flow<List<BankAccountEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAccount(account: BankAccountEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAccounts(accounts: List<BankAccountEntity>)
}
