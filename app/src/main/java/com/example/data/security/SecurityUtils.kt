package com.example.data.security

import com.example.data.entity.UserEntity
import com.example.data.model.UserRole
import java.security.MessageDigest

object SecurityUtils {
  private const val DEFAULT_SALT = "CharlesEnterpriseAuthSalt@2025#Secured"

  fun hashPassword(password: String, salt: String = DEFAULT_SALT): String {
    val md = MessageDigest.getInstance("SHA-256")
    val input = "$salt:$password:$salt"
    val digest = md.digest(input.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }

  fun verifyPassword(password: String, storedHash: String, salt: String = DEFAULT_SALT): Boolean {
    val computed = hashPassword(password, salt)
    return MessageDigest.isEqual(computed.toByteArray(Charsets.UTF_8), storedHash.toByteArray(Charsets.UTF_8))
  }

  fun isAdministrativeRole(role: UserRole): Boolean {
    return when (role) {
      UserRole.SUPER_ADMIN,
      UserRole.FINANCE_MANAGER,
      UserRole.INVENTORY_MANAGER,
      UserRole.ORDER_MANAGER,
      UserRole.DISPATCH_MANAGER -> true
      UserRole.CUSTOMER,
      UserRole.RIDER,
      UserRole.SUPPORT -> false
    }
  }

  fun isFinanceAuthorized(role: UserRole): Boolean {
    return role == UserRole.SUPER_ADMIN || role == UserRole.FINANCE_MANAGER
  }

  fun isInventoryAuthorized(role: UserRole): Boolean {
    return role == UserRole.SUPER_ADMIN || role == UserRole.INVENTORY_MANAGER
  }

  fun isDispatchAuthorized(role: UserRole): Boolean {
    return role == UserRole.SUPER_ADMIN || role == UserRole.DISPATCH_MANAGER || role == UserRole.RIDER
  }
}
