package com.example.security

import android.content.Context
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.UserEntity
import com.example.data.model.UserRole
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

sealed class AdminAuthResult {
  data class Success(val user: UserEntity) : AdminAuthResult()
  data class Failure(val reason: String) : AdminAuthResult()
  object NotConfigured : AdminAuthResult()
}

class AdminSecurityManager(private val context: Context) {
  private val prefs = context.getSharedPreferences("charles_admin_security_vault", Context.MODE_PRIVATE)

  fun isConfigured(): Boolean {
    return prefs.getBoolean("is_configured", false)
  }

  fun getAdminEmail(): String? {
    return prefs.getString("admin_email", null)
  }

  fun setupOwnerCredentials(
    email: String,
    passwordPlain: String,
    twoFactorPin: String,
    recoveryToken: String,
    role: UserRole = UserRole.SUPER_ADMIN
  ): Result<UserEntity> {
    if (!email.contains("@") || !email.contains(".")) {
      return Result.failure(IllegalArgumentException("Please enter a valid administrative email address."))
    }
    if (passwordPlain.length < 8) {
      return Result.failure(IllegalArgumentException("Administrative password must be at least 8 characters long."))
    }
    if (twoFactorPin.length != 6 || !twoFactorPin.all { it.isDigit() }) {
      return Result.failure(IllegalArgumentException("Secondary factor (2FA PIN) must be exactly 6 numeric digits."))
    }
    if (recoveryToken.length < 6) {
      return Result.failure(IllegalArgumentException("Emergency recovery token must be at least 6 characters."))
    }

    val salt = generateSalt()
    val passwordHash = hashWithSalt(passwordPlain, salt)
    val pinHash = hashWithSalt(twoFactorPin, salt)
    val recoveryHash = hashWithSalt(recoveryToken, salt)

    val adminUserId = "admin_owner_${UUID.randomUUID().toString().take(8)}"

    prefs.edit()
      .putBoolean("is_configured", true)
      .putString("admin_id", adminUserId)
      .putString("admin_email", email.trim().lowercase())
      .putString("salt", salt)
      .putString("password_hash", passwordHash)
      .putString("pin_hash", pinHash)
      .putString("recovery_hash", recoveryHash)
      .putString("role", role.name)
      .putLong("setup_timestamp", System.currentTimeMillis())
      .apply()

    val adminUser = UserEntity(
      id = adminUserId,
      fullName = "Charles Enterprise Principal",
      email = email.trim().lowercase(),
      phone = "+234 800 CHARLES",
      role = role,
      address = "Charles Enterprise Executive Suite, Victoria Island, Lagos"
    )

    return Result.success(adminUser)
  }

  fun authenticate(
    email: String,
    passwordPlain: String,
    twoFactorPin: String
  ): AdminAuthResult {
    if (!isConfigured()) {
      return AdminAuthResult.NotConfigured
    }

    val storedEmail = prefs.getString("admin_email", "") ?: ""
    val salt = prefs.getString("salt", "") ?: ""
    val storedPassHash = prefs.getString("password_hash", "") ?: ""
    val storedPinHash = prefs.getString("pin_hash", "") ?: ""
    val roleName = prefs.getString("role", UserRole.SUPER_ADMIN.name) ?: UserRole.SUPER_ADMIN.name
    val adminId = prefs.getString("admin_id", "admin_owner_primary") ?: "admin_owner_primary"

    if (!storedEmail.equals(email.trim(), ignoreCase = true)) {
      return AdminAuthResult.Failure("Administrator record not found for this email.")
    }

    val incomingPassHash = hashWithSalt(passwordPlain, salt)
    if (incomingPassHash != storedPassHash) {
      return AdminAuthResult.Failure("Invalid administrative credentials. Attempt logged.")
    }

    val incomingPinHash = hashWithSalt(twoFactorPin, salt)
    if (incomingPinHash != storedPinHash) {
      return AdminAuthResult.Failure("Secondary verification failed. Invalid 6-digit 2FA token.")
    }

    val role = try {
      UserRole.valueOf(roleName)
    } catch (e: Exception) {
      UserRole.SUPER_ADMIN
    }

    val authenticatedUser = UserEntity(
      id = adminId,
      fullName = "Charles Enterprise Principal",
      email = storedEmail,
      phone = "+234 800 CHARLES",
      role = role,
      address = "Charles Enterprise Executive Suite, Victoria Island, Lagos"
    )

    return AdminAuthResult.Success(authenticatedUser)
  }

  fun recoverAccount(
    recoveryToken: String,
    newPasswordPlain: String,
    newTwoFactorPin: String
  ): Result<Unit> {
    if (!isConfigured()) {
      return Result.failure(IllegalStateException("No administrator credentials have been initialized yet."))
    }
    val salt = prefs.getString("salt", "") ?: ""
    val storedRecoveryHash = prefs.getString("recovery_hash", "") ?: ""

    val incomingRecoveryHash = hashWithSalt(recoveryToken, salt)
    if (incomingRecoveryHash != storedRecoveryHash) {
      return Result.failure(IllegalArgumentException("Invalid emergency recovery token."))
    }

    if (newPasswordPlain.length < 8) {
      return Result.failure(IllegalArgumentException("New password must be at least 8 characters long."))
    }
    if (newTwoFactorPin.length != 6 || !newTwoFactorPin.all { it.isDigit() }) {
      return Result.failure(IllegalArgumentException("New 2FA PIN must be exactly 6 digits."))
    }

    val newSalt = generateSalt()
    val newPassHash = hashWithSalt(newPasswordPlain, newSalt)
    val newPinHash = hashWithSalt(newTwoFactorPin, newSalt)
    val newRecoveryHash = hashWithSalt(recoveryToken, newSalt) // keep same recovery token

    prefs.edit()
      .putString("salt", newSalt)
      .putString("password_hash", newPassHash)
      .putString("pin_hash", newPinHash)
      .putString("recovery_hash", newRecoveryHash)
      .putLong("recovered_timestamp", System.currentTimeMillis())
      .apply()

    return Result.success(Unit)
  }

  private fun generateSalt(): String {
    val random = SecureRandom()
    val bytes = ByteArray(16)
    random.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
  }

  private fun hashWithSalt(input: String, salt: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest((salt + input + "charles_enterprise_auth_pepper").toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }
}
