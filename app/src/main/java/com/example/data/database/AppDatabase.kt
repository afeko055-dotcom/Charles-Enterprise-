package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AuditLogDao
import com.example.data.dao.BankAccountDao
import com.example.data.dao.ContactDao
import com.example.data.dao.DeliveryDao
import com.example.data.dao.LedgerDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.OrderDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.UserDao
import com.example.data.dao.VehicleDao
import com.example.data.dao.WebhookEventDao
import com.example.data.dao.WithdrawalDao
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
import com.example.data.model.BodyType
import com.example.data.model.FuelType
import com.example.data.model.TransmissionType
import com.example.data.model.UserRole
import com.example.data.model.VehicleCondition
import com.example.data.model.VehicleStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    VehicleEntity::class,
    SavedVehicleEntity::class,
    OrderEntity::class,
    PaymentEntity::class,
    WebhookEventEntity::class,
    LedgerEntryEntity::class,
    WithdrawalEntity::class,
    BankAccountEntity::class,
    DeliveryEntity::class,
    DeliveryLocationEntity::class,
    NotificationEntity::class,
    ContactMessageEntity::class,
    AuditLogEntity::class
  ],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun vehicleDao(): VehicleDao
  abstract fun orderDao(): OrderDao
  abstract fun paymentDao(): PaymentDao
  abstract fun webhookEventDao(): WebhookEventDao
  abstract fun ledgerDao(): LedgerDao
  abstract fun withdrawalDao(): WithdrawalDao
  abstract fun deliveryDao(): DeliveryDao
  abstract fun notificationDao(): NotificationDao
  abstract fun contactDao(): ContactDao
  abstract fun auditLogDao(): AuditLogDao
  abstract fun userDao(): UserDao
  abstract fun bankAccountDao(): BankAccountDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "apex_motors_db"
        )
          .addCallback(DatabaseSeedCallback(scope))
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class DatabaseSeedCallback(
    private val scope: CoroutineScope
  ) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          seedDatabase(database)
        }
      }
    }

    private suspend fun seedDatabase(db: AppDatabase) {
      // 1. Seed Users
      val users = listOf(
        UserEntity(
          id = "user_cust_1",
          fullName = "Adebayo Williams",
          email = "adebayo@example.com",
          phone = "+234 803 555 0192",
          role = UserRole.CUSTOMER,
          address = "Bourdillon Rd, Ikoyi, Lagos"
        ),
        UserEntity(
          id = "user_rider_1",
          fullName = "Chinedu Eze",
          email = "chinedu.eze@charleslogistics.ng",
          phone = "+234 812 400 9988",
          role = UserRole.RIDER,
          address = "Charles Enterprise Logistics Hub, Lekki, Lagos"
        ),
        UserEntity(
          id = "user_admin_1",
          fullName = "David Vance",
          email = "david.vance@charlesenterprise.com",
          phone = "+234 809 111 2233",
          role = UserRole.SUPER_ADMIN,
          address = "Charles Tower, Victoria Island, Lagos"
        ),
        UserEntity(
          id = "user_fin_1",
          fullName = "Fatima Bello",
          email = "fatima.bello@charlesenterprise.com",
          phone = "+234 805 777 8899",
          role = UserRole.FINANCE_MANAGER,
          address = "Charles Tower, Victoria Island, Lagos"
        ),
        UserEntity(
          id = "user_inv_1",
          fullName = "Sarah Adams",
          email = "sarah.adams@charlesenterprise.com",
          phone = "+234 802 333 4455",
          role = UserRole.INVENTORY_MANAGER,
          address = "Lekki Showroom, Lagos"
        )
      )
      db.userDao().insertUsers(users)

      // 2. Seed Bank Accounts for Withdrawals
      val bankAccounts = listOf(
        BankAccountEntity(
          id = "bank_gtb_1",
          bankName = "Guaranty Trust Bank (GTBank)",
          bankCode = "058",
          accountNumberMasked = "******4819",
          accountHolderName = "CHARLES ENTERPRISE NIGERIA LTD",
          recipientId = "RCP_gtb_9812491",
          isDefault = true
        ),
        BankAccountEntity(
          id = "bank_acc_1",
          bankName = "Access Bank Plc",
          bankCode = "044",
          accountNumberMasked = "******1023",
          accountHolderName = "CHARLES ENTERPRISE OPERATIONS",
          recipientId = "RCP_acc_3391821",
          isDefault = false
        ),
        BankAccountEntity(
          id = "bank_zenith_1",
          bankName = "Zenith Bank Plc",
          bankCode = "057",
          accountNumberMasked = "******7741",
          accountHolderName = "CHARLES ENTERPRISE ESCROW",
          recipientId = "RCP_zen_7719200",
          isDefault = false
        )
      )
      db.bankAccountDao().insertAccounts(bankAccounts)

      // 3. Seed Vehicles (price in kobo: e.g. 185,000,000 NGN = 18500000000 kobo)
      val vehicles = listOf(
        VehicleEntity(
          id = "veh_amg_g63",
          brand = "Mercedes-AMG",
          model = "G63 Bi-Turbo Magno Edition",
          year = 2024,
          priceKobo = 285_000_000_00L, // 285M NGN
          currency = "NGN",
          mileageKm = 3400,
          transmission = TransmissionType.AUTOMATIC,
          fuelType = FuelType.PETROL,
          bodyType = BodyType.SUV,
          color = "Night Black Magno",
          condition = VehicleCondition.BRAND_NEW,
          description = "Handcrafted 4.0L V8 Biturbo producing 577 hp. Finished in exclusive Night Black Magno with G Manufaktur Diamond Stitched Bengal Red Nappa Leather, carbon fiber interior trim, AMG ceramic composite brakes, and 22-inch forged cross-spoke wheels.",
          location = "Victoria Island Flagship Hub, Lagos",
          status = VehicleStatus.AVAILABLE,
          featured = true,
          vin = "WDB9632481A902847",
          engineSpecs = "4.0L V8 Biturbo Handcrafted AMG",
          horsepower = 577,
          acceleration0to100 = 4.5f,
          imageResName = "img_suv_luxury"
        ),
        VehicleEntity(
          id = "veh_porsche_911",
          brand = "Porsche",
          model = "911 GT3 Touring Package",
          year = 2024,
          priceKobo = 320_000_000_00L, // 320M NGN
          currency = "NGN",
          mileageKm = 1200,
          transmission = TransmissionType.DUAL_CLUTCH,
          fuelType = FuelType.PETROL,
          bodyType = BodyType.COUPE,
          color = "Shark Blue Metallic",
          condition = VehicleCondition.CERTIFIED_PRE_OWNED,
          description = "Naturally aspirated 4.0L flat-six revving to an exhilarating 9,000 RPM. Equipped with 7-speed PDK transmission, front axle lift system, Porsche Carbon Ceramic Brakes (PCCB), full carbon bucket seats, and lightweight carbon roof.",
          location = "Lekki Phase 1 Showroom, Lagos",
          status = VehicleStatus.AVAILABLE,
          featured = true,
          vin = "WP0AD2A98NS294819",
          engineSpecs = "4.0L Naturally Aspirated Flat-Six Boxer",
          horsepower = 502,
          acceleration0to100 = 3.2f,
          imageResName = "img_coupe_supercar"
        ),
        VehicleEntity(
          id = "veh_rolls_ghost",
          brand = "Rolls-Royce",
          model = "Ghost Extended Wheelbase",
          year = 2023,
          priceKobo = 490_000_000_00L, // 490M NGN
          currency = "NGN",
          mileageKm = 4800,
          transmission = TransmissionType.AUTOMATIC,
          fuelType = FuelType.PETROL,
          bodyType = BodyType.SEDAN,
          color = "Diamond Black & Silver Duo-Tone",
          condition = VehicleCondition.FOREIGN_USED,
          description = "The pinnacle of automotive luxury. Features Planar suspension system, Starlight Headliner with shooting star sequence, rear theatre entertainment, champagne chiller with crystal flutes, and effortless twin-turbo V12 whispering power.",
          location = "Apex VIP Vault, Ikoyi, Lagos",
          status = VehicleStatus.AVAILABLE,
          featured = true,
          vin = "SCA664D07LUX99182",
          engineSpecs = "6.75L Twin-Turbocharged V12",
          horsepower = 563,
          acceleration0to100 = 4.8f,
          imageResName = "img_hero_car"
        ),
        VehicleEntity(
          id = "veh_range_rover_sv",
          brand = "Land Rover",
          model = "Range Rover SV Autobiography LWB",
          year = 2024,
          priceKobo = 310_000_000_00L, // 310M NGN
          currency = "NGN",
          mileageKm = 2100,
          transmission = TransmissionType.AUTOMATIC,
          fuelType = FuelType.PETROL,
          bodyType = BodyType.SUV,
          color = "British Racing Green Satin",
          condition = VehicleCondition.BRAND_NEW,
          description = "Exclusive SV Serenity theme with bespoke ceramic controls, 24-way heated and cooled massage electric rear executive class comfort-plus seats, Meridian Signature Sound System (35 speakers, 1600W), and all-wheel steering.",
          location = "Victoria Island Flagship Hub, Lagos",
          status = VehicleStatus.AVAILABLE,
          featured = false,
          vin = "SALWR2V42PA884719",
          engineSpecs = "4.4L Twin-Turbo V8",
          horsepower = 523,
          acceleration0to100 = 4.6f,
          imageResName = "img_suv_luxury"
        ),
        VehicleEntity(
          id = "veh_bmw_m8",
          brand = "BMW",
          model = "M8 Competition Gran Coupe",
          year = 2023,
          priceKobo = 215_000_000_00L, // 215M NGN
          currency = "NGN",
          mileageKm = 8900,
          transmission = TransmissionType.AUTOMATIC,
          fuelType = FuelType.PETROL,
          bodyType = BodyType.SEDAN,
          color = "Frozen Marina Bay Blue",
          condition = VehicleCondition.FOREIGN_USED,
          description = "M xDrive intelligent all-wheel drive with 2WD rear-wheel drift mode. Carbon fiber exterior aero package, Bowers & Wilkins Diamond Surround Sound, and M Carbon Ceramic Braking system.",
          location = "Abuja Central Showroom, FCT",
          status = VehicleStatus.AVAILABLE,
          featured = false,
          vin = "WBAAE8C55NCM18293",
          engineSpecs = "4.4L M TwinPower Turbo V8",
          horsepower = 617,
          acceleration0to100 = 3.0f,
          imageResName = "img_hero_car"
        ),
        VehicleEntity(
          id = "veh_ferrari_roma",
          brand = "Ferrari",
          model = "Roma Coupe V8 Turbo",
          year = 2023,
          priceKobo = 365_000_000_00L, // 365M NGN
          currency = "NGN",
          mileageKm = 3100,
          transmission = TransmissionType.DUAL_CLUTCH,
          fuelType = FuelType.PETROL,
          bodyType = BodyType.COUPE,
          color = "Rosso Corsa Red",
          condition = VehicleCondition.CERTIFIED_PRE_OWNED,
          description = "La Nuova Dolce Vita. 3.9L 90-degree turbocharged V8 paired with an 8-speed dual-clutch transmission derived from the SF90 Stradale. Features passenger display, active aerodynamics, and full Alcantara sports interior.",
          location = "Lekki Phase 1 Showroom, Lagos",
          status = VehicleStatus.AVAILABLE,
          featured = true,
          vin = "ZFF98RMA7P0293817",
          engineSpecs = "3.9L Twin-Turbocharged 90° V8",
          horsepower = 612,
          acceleration0to100 = 3.4f,
          imageResName = "img_coupe_supercar"
        )
      )
      db.vehicleDao().insertVehicles(vehicles)

      // 4. Seed Audit Log of showroom launch
      val auditLogs = listOf(
        AuditLogEntity(
          id = "audit_init_1",
          actorId = "user_admin_1",
          actorName = "David Vance",
          actorRole = "SUPER_ADMIN",
          action = "INVENTORY_INITIALIZED",
          details = "Charles Enterprise flagship automotive vault initialized with certified luxury inventory.",
          targetId = "SYSTEM",
          ipAddress = "197.210.226.14"
        )
      )
      for (log in auditLogs) {
        db.auditLogDao().insertLog(log)
      }
    }
  }
}
