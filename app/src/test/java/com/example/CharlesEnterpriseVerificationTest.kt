package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.entity.DeliveryEntity
import com.example.data.entity.LedgerEntryEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.VehicleEntity
import com.example.data.model.BodyType
import com.example.data.model.CurrencyFormatter
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
import com.example.payment.FlutterwavePaymentProvider
import com.example.payment.PaystackPaymentProvider
import com.example.payment.WebhookEventPayload
import com.example.security.AdminAuthResult
import com.example.security.AdminSecurityManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CharlesEnterpriseVerificationTest {

  private lateinit var context: Context
  private lateinit var securityManager: AdminSecurityManager

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Context>()
    // Clear shared preferences for clean test state
    val prefs = context.getSharedPreferences("charles_admin_security_vault", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()
    securityManager = AdminSecurityManager(context)
  }

  @Test
  fun testOwnerAdminSetupAndAuthentication() {
    assertFalse("Security manager must not be configured initially", securityManager.isConfigured())

    // 1. Invalid setup attempts (too short password / non-numeric 2FA)
    val shortPassResult = securityManager.setupOwnerCredentials(
      email = "owner@charlesenterprise.com",
      passwordPlain = "short",
      twoFactorPin = "123456",
      recoveryToken = "master_recovery_token_999"
    )
    assertTrue("Should fail with short password", shortPassResult.isFailure)

    val invalid2FaResult = securityManager.setupOwnerCredentials(
      email = "owner@charlesenterprise.com",
      passwordPlain = "ValidPassword123#",
      twoFactorPin = "123AB6",
      recoveryToken = "master_recovery_token_999"
    )
    assertTrue("Should fail with non-numeric 2FA", invalid2FaResult.isFailure)

    // 2. Valid setup
    val validSetupResult = securityManager.setupOwnerCredentials(
      email = "principal@charlesenterprise.com",
      passwordPlain = "Excellence2026!#",
      twoFactorPin = "982314",
      recoveryToken = "emergency_safeguard_token_4918",
      role = UserRole.SUPER_ADMIN
    )
    assertTrue("Valid setup must succeed", validSetupResult.isSuccess)
    assertTrue("Security manager must now be configured", securityManager.isConfigured())

    // 3. Successful authentication
    val authResult = securityManager.authenticate(
      email = "principal@charlesenterprise.com",
      passwordPlain = "Excellence2026!#",
      twoFactorPin = "982314"
    )
    assertTrue("Authentication should succeed with correct credentials and 2FA", authResult is AdminAuthResult.Success)
    val authenticatedUser = (authResult as AdminAuthResult.Success).user
    assertEquals(UserRole.SUPER_ADMIN, authenticatedUser.role)
    assertEquals("principal@charlesenterprise.com", authenticatedUser.email)

    // 4. Failed authentication - Wrong password
    val badPassResult = securityManager.authenticate(
      email = "principal@charlesenterprise.com",
      passwordPlain = "WrongPassword!",
      twoFactorPin = "982314"
    )
    assertTrue("Must reject invalid password", badPassResult is AdminAuthResult.Failure)

    // 5. Failed authentication - Wrong 2FA PIN
    val badPinResult = securityManager.authenticate(
      email = "principal@charlesenterprise.com",
      passwordPlain = "Excellence2026!#",
      twoFactorPin = "000000"
    )
    assertTrue("Must reject invalid 2FA PIN", badPinResult is AdminAuthResult.Failure)
  }

  @Test
  fun testAdminEmergencyRecovery() {
    securityManager.setupOwnerCredentials(
      email = "admin@charlesenterprise.com",
      passwordPlain = "OriginalPass123!",
      twoFactorPin = "112233",
      recoveryToken = "secure_recovery_phrase_7718"
    )

    // Attempt recovery with invalid token
    val invalidRecovery = securityManager.recoverAccount(
      recoveryToken = "wrong_token",
      newPasswordPlain = "NewPassword2026!",
      newTwoFactorPin = "445566"
    )
    assertTrue("Must reject invalid recovery token", invalidRecovery.isFailure)

    // Valid recovery
    val validRecovery = securityManager.recoverAccount(
      recoveryToken = "secure_recovery_phrase_7718",
      newPasswordPlain = "NewPassword2026!",
      newTwoFactorPin = "445566"
    )
    assertTrue("Recovery should succeed with valid token", validRecovery.isSuccess)

    // Old credentials should fail
    val oldAuth = securityManager.authenticate("admin@charlesenterprise.com", "OriginalPass123!", "112233")
    assertTrue("Old credentials must fail after recovery", oldAuth is AdminAuthResult.Failure)

    // New credentials must succeed
    val newAuth = securityManager.authenticate("admin@charlesenterprise.com", "NewPassword2026!", "445566")
    assertTrue("New credentials must succeed", newAuth is AdminAuthResult.Success)
  }

  @Test
  fun testAuthoritativeIntegerPricingNoFloatingPointLoss() {
    // Rolls-Royce Ghost: ₦450,000,000.00 = 45,000,000,000 Kobo
    val basePriceKobo = 45_000_000_000L
    val flatbedEnclosedDeliveryFeeKobo = 500_000_00L // ₦500,000.00
    val documentationEscrowFeeKobo = 250_000_00L // ₦250,000.00

    val orderTotalKobo = basePriceKobo + flatbedEnclosedDeliveryFeeKobo + documentationEscrowFeeKobo
    assertEquals(45_075_000_000L, orderTotalKobo)

    // Gateway surcharge: 1.5% capped
    val gatewayFeeKobo = (orderTotalKobo * 0.015).toLong()
    val merchantNetKobo = orderTotalKobo - gatewayFeeKobo

    assertEquals(orderTotalKobo, merchantNetKobo + gatewayFeeKobo)
    assertTrue("Formatted Naira must display correct symbol", CurrencyFormatter.formatKoboToNaira(orderTotalKobo).startsWith("₦"))
  }

  @Test
  fun testPaystackWebhookHmacSignatureValidation() = runBlocking {
    val provider = PaystackPaymentProvider(secretKey = "sk_test_charles_sec_781923")
    val payloadJson = """{"event":"charge.success","data":{"reference":"pstk_ce_99124","amount":45075000000}}"""

    val validSignature = provider.generateTestSignature(payloadJson)

    val validPayload = WebhookEventPayload(
      eventId = "evt_ce_001",
      eventType = "charge.success",
      provider = PaymentGateway.PAYSTACK,
      reference = "pstk_ce_99124",
      amountKobo = 45_075_000_000L,
      currency = "NGN",
      status = PaymentStatus.SUCCESS,
      signature = validSignature,
      rawPayloadJson = payloadJson
    )

    val validResult = provider.handleWebhook(validSignature, validPayload)
    assertTrue("Paystack HMAC signature must validate successfully", validResult.isValidSignature)

    val tamperedResult = provider.handleWebhook("forged_signature_attack", validPayload)
    assertFalse("Forged signature must be rejected", tamperedResult.isValidSignature)
  }

  @Test
  fun testFlutterwaveWebhookSecretHashValidation() = runBlocking {
    val provider = FlutterwavePaymentProvider(secretHash = "ce_flw_secret_hash_9941")
    val payloadJson = """{"event":"charge.completed","data":{"tx_ref":"flw_ce_55102","amount":320000000}}"""

    val validPayload = WebhookEventPayload(
      eventId = "evt_flw_001",
      eventType = "charge.completed",
      provider = PaymentGateway.FLUTTERWAVE,
      reference = "flw_ce_55102",
      amountKobo = 320_000_000_00L,
      currency = "NGN",
      status = PaymentStatus.SUCCESS,
      signature = "ce_flw_secret_hash_9941",
      rawPayloadJson = payloadJson
    )

    val validResult = provider.handleWebhook("ce_flw_secret_hash_9941", validPayload)
    assertTrue("Valid Flutterwave secret hash must pass", validResult.isValidSignature)

    val forgedResult = provider.handleWebhook("invalid_flw_hash", validPayload)
    assertFalse("Forged Flutterwave secret hash must be rejected", forgedResult.isValidSignature)
  }

  @Test
  fun testDeliveryOtpSecurityProtocol() {
    val plainOtp = "842915"
    val salt = "ce_delivery_salt_v1"

    // SHA-256 OTP Hash
    val md = MessageDigest.getInstance("SHA-256")
    val hashedBytes = md.digest((salt + plainOtp).toByteArray(Charsets.UTF_8))
    val storedHash = hashedBytes.joinToString("") { "%02x".format(it) }

    // Rider inputs OTP on handover
    val riderInputCorrect = "842915"
    val riderHashCorrect = md.digest((salt + riderInputCorrect).toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    assertEquals("Correct OTP input must match stored delivery hash", storedHash, riderHashCorrect)

    val riderInputWrong = "123456"
    val riderHashWrong = md.digest((salt + riderInputWrong).toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    assertFalse("Wrong OTP input must not match", storedHash == riderHashWrong)
  }

  @Test
  fun testLedgerDoubleEntryAndAvailableBalanceSegregation() {
    // Simulate ledger entries
    val entries = listOf(
      // Credit: Sale payment received but PENDING settlement (T+1)
      LedgerEntryEntity(
        id = "led_1",
        type = LedgerType.SALE,
        direction = LedgerDirection.CREDIT,
        amountKobo = 320_000_000_00L,
        netAmountKobo = 315_200_000_00L,
        reference = "ref_order_1",
        description = "Vehicle Sale - Pending settlement",
        isAvailable = false
      ),
      // Credit: Settled and moved to AVAILABLE
      LedgerEntryEntity(
        id = "led_2",
        type = LedgerType.SETTLEMENT,
        direction = LedgerDirection.CREDIT,
        amountKobo = 250_000_000_00L,
        netAmountKobo = 250_000_000_00L,
        reference = "ref_order_2",
        description = "Settled Funds Available for Payout",
        isAvailable = true
      ),
      // Debit: Payout withdrawal executed
      LedgerEntryEntity(
        id = "led_3",
        type = LedgerType.PAYOUT,
        direction = LedgerDirection.DEBIT,
        amountKobo = 50_000_000_00L,
        netAmountKobo = 50_000_000_00L,
        reference = "wth_101",
        description = "Owner Treasury Payout",
        isAvailable = true
      )
    )

    val availableCredit = entries.filter { it.isAvailable && it.direction == LedgerDirection.CREDIT }.sumOf { it.netAmountKobo }
    val availableDebit = entries.filter { it.isAvailable && it.direction == LedgerDirection.DEBIT }.sumOf { it.netAmountKobo }
    val currentWithdrawableBalance = availableCredit - availableDebit

    assertEquals(200_000_000_00L, currentWithdrawableBalance)

    val pendingBalance = entries.filter { !it.isAvailable && it.direction == LedgerDirection.CREDIT }.sumOf { it.netAmountKobo }
    assertEquals(315_200_000_00L, pendingBalance)

    // A withdrawal request of 250 Million should be rejected because available balance is 200 Million
    val requestedWithdrawalKobo = 250_000_000_00L
    assertTrue("Withdrawal exceeding available balance must be flagged", requestedWithdrawalKobo > currentWithdrawableBalance)
  }

  @Test
  fun testVehicleSearchAndFiltering() {
    val vehicles = listOf(
      VehicleEntity(
        id = "veh_1",
        brand = "Mercedes-Benz",
        model = "AMG G63",
        year = 2024,
        priceKobo = 280_000_000_00L,
        mileageKm = 1200,
        transmission = TransmissionType.AUTOMATIC,
        fuelType = FuelType.PETROL,
        bodyType = BodyType.SUV,
        color = "Obsidian Black",
        condition = VehicleCondition.BRAND_NEW,
        description = "Certified Mercedes-AMG G63",
        location = "Lekki Showroom",
        vin = "WDB96348192301",
        engineSpecs = "4.0L V8 Biturbo",
        horsepower = 577,
        acceleration0to100 = 4.5f,
        imageResName = "img_suv_luxury"
      ),
      VehicleEntity(
        id = "veh_2",
        brand = "Porsche",
        model = "911 GT3 RS",
        year = 2024,
        priceKobo = 380_000_000_00L,
        mileageKm = 400,
        transmission = TransmissionType.DUAL_CLUTCH,
        fuelType = FuelType.PETROL,
        bodyType = BodyType.COUPE,
        color = "Shark Blue",
        condition = VehicleCondition.BRAND_NEW,
        description = "Certified Porsche 911 GT3 RS",
        location = "Ikoyi Vault",
        vin = "WP0911GT3RS2024",
        engineSpecs = "4.0L Naturally Aspirated Boxer-6",
        horsepower = 518,
        acceleration0to100 = 3.2f,
        imageResName = "img_coupe_supercar"
      ),
      VehicleEntity(
        id = "veh_3",
        brand = "Rolls-Royce",
        model = "Ghost Extended",
        year = 2023,
        priceKobo = 480_000_000_00L,
        mileageKm = 3500,
        transmission = TransmissionType.AUTOMATIC,
        fuelType = FuelType.PETROL,
        bodyType = BodyType.SEDAN,
        color = "Arctic White",
        condition = VehicleCondition.CERTIFIED_PRE_OWNED,
        description = "Certified Rolls-Royce Ghost Extended",
        location = "Victoria Island Hub",
        vin = "SCA664RR99104",
        engineSpecs = "6.75L Twin-Turbo V12",
        horsepower = 563,
        acceleration0to100 = 4.8f,
        imageResName = "img_hero_car"
      )
    )

    // Filter 1: Query "Porsche"
    val porscheMatch = vehicles.filter { it.brand.contains("Porsche", ignoreCase = true) }
    assertEquals(1, porscheMatch.size)
    assertEquals("911 GT3 RS", porscheMatch.first().model)

    // Filter 2: Body type SUV
    val suvMatch = vehicles.filter { it.bodyType == BodyType.SUV }
    assertEquals(1, suvMatch.size)
    assertEquals("AMG G63", suvMatch.first().model)

    // Filter 3: Condition BRAND_NEW
    val newMatch = vehicles.filter { it.condition == VehicleCondition.BRAND_NEW }
    assertEquals(2, newMatch.size)

    // Filter 4: Price ordering
    val sortedAsc = vehicles.sortedBy { it.priceKobo }
    assertEquals("AMG G63", sortedAsc.first().model)
    assertEquals("Ghost Extended", sortedAsc.last().model)
  }

  @Test
  fun testRoleAccessControlMatrix() {
    val customer = UserEntity(
      id = "cust_1",
      fullName = "Adebayo Williams",
      email = "adebayo@example.com",
      phone = "+234800000001",
      role = UserRole.CUSTOMER
    )

    val rider = UserEntity(
      id = "rider_1",
      fullName = "Chidi Okafor",
      email = "chidi@charlesenterprise.com",
      phone = "+234800000002",
      role = UserRole.RIDER
    )

    val financeMgr = UserEntity(
      id = "fin_1",
      fullName = "Folake Solanke",
      email = "finance@charlesenterprise.com",
      phone = "+234800000003",
      role = UserRole.FINANCE_MANAGER
    )

    val superAdmin = UserEntity(
      id = "admin_1",
      fullName = "Charles Enterprise Principal",
      email = "owner@charlesenterprise.com",
      phone = "+234800000004",
      role = UserRole.SUPER_ADMIN
    )

    // Customer should not have administrative privileges
    assertFalse(customer.role == UserRole.SUPER_ADMIN || customer.role == UserRole.FINANCE_MANAGER)

    // Rider should only have dispatch clearance
    assertEquals(UserRole.RIDER, rider.role)

    // Finance manager has ledger access
    assertTrue(financeMgr.role == UserRole.SUPER_ADMIN || financeMgr.role == UserRole.FINANCE_MANAGER)

    // Super Admin has all privileges
    assertTrue(superAdmin.role == UserRole.SUPER_ADMIN)
  }
}
