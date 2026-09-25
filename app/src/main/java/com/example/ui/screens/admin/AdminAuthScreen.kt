package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserEntity
import com.example.data.model.UserRole
import com.example.security.AdminAuthResult
import com.example.security.AdminSecurityManager
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import com.example.viewmodel.MarketplaceViewModel

@Composable
fun AdminAuthScreen(
  viewModel: MarketplaceViewModel,
  onAuthenticated: (UserEntity) -> Unit,
  onExit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val securityManager = remember { AdminSecurityManager(context) }
  var isConfigured by remember { mutableStateOf(securityManager.isConfigured()) }

  // Auth Inputs
  var email by remember { mutableStateOf(securityManager.getAdminEmail() ?: "") }
  var password by remember { mutableStateOf("") }
  var twoFactorPin by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  // Setup Inputs
  var setupEmail by remember { mutableStateOf("") }
  var setupPassword by remember { mutableStateOf("") }
  var setupConfirmPassword by remember { mutableStateOf("") }
  var setupTwoFactorPin by remember { mutableStateOf("") }
  var setupRecoveryToken by remember { mutableStateOf("") }

  // Recovery Mode
  var isRecoveryMode by remember { mutableStateOf(false) }
  var recoveryToken by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var newTwoFactorPin by remember { mutableStateOf("") }

  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .verticalScroll(rememberScrollState())
      .padding(24.dp)
      .testTag("admin_auth_screen"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Bar with back affordance
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onExit) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Return to Showroom",
          tint = SlateMuted
        )
      }

      Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF161F30),
        border = BorderStroke(1.dp, CarbonBorder)
      ) {
        Text(
          text = "ROUTE: /admin",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = AmberGold,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Brand Emblem
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = AmberGold.copy(alpha = 0.15f),
      border = BorderStroke(1.5.dp, AmberGold),
      modifier = Modifier.size(64.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Enterprise Security Shield",
          tint = AmberGold,
          modifier = Modifier.size(34.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "CHARLES ENTERPRISE",
      fontSize = 20.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 3.sp,
      color = PlatinumText
    )

    Text(
      text = "ADMINISTRATIVE CONTROL ACCESS",
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 2.sp,
      color = AmberGold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Protected zone. Cryptographic salted verification & 2FA strictly enforced.",
      fontSize = 12.sp,
      color = SlateMuted
    )

    Spacer(modifier = Modifier.height(24.dp))

    if (errorMessage != null) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = CrimsonAlert.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, CrimsonAlert),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = errorMessage!!,
          color = CrimsonAlert,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(12.dp)
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    if (successMessage != null) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = EmeraldSuccess.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, EmeraldSuccess),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = successMessage!!,
          color = EmeraldSuccess,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(12.dp)
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Three states: 1. Owner Setup, 2. Normal Login, 3. Emergency Recovery
    if (!isConfigured) {
      // OWNER SETUP MODE
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Key,
              contentDescription = null,
              tint = AmberGold,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Owner Security Configuration",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = PlatinumText
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "No default or hardcoded credentials exist. Establish your authoritative administrator account below:",
            fontSize = 12.sp,
            color = SlateMuted
          )

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = setupEmail,
            onValueChange = { setupEmail = it },
            label = { Text("Owner Email Address", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AmberGold) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_setup_email"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = setupPassword,
            onValueChange = { setupPassword = it },
            label = { Text("Master Admin Password (min 8 chars)", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberGold) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = null,
                  tint = SlateMuted
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_setup_password"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = setupTwoFactorPin,
            onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) setupTwoFactorPin = it },
            label = { Text("6-Digit 2FA Security PIN", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AmberGold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_setup_2fa_pin"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = setupRecoveryToken,
            onValueChange = { setupRecoveryToken = it },
            label = { Text("Emergency Recovery Passphrase", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = AmberGold) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_setup_recovery_token"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              errorMessage = null
              val setupResult = securityManager.setupOwnerCredentials(
                email = setupEmail,
                passwordPlain = setupPassword,
                twoFactorPin = setupTwoFactorPin,
                recoveryToken = setupRecoveryToken,
                role = UserRole.SUPER_ADMIN
              )
              setupResult.fold(
                onSuccess = { createdUser ->
                  isConfigured = true
                  viewModel.switchUser(createdUser)
                  viewModel.recordAuditLog(
                    action = "OWNER_ADMIN_INITIALIZED",
                    details = "Owner established primary administrator credentials with salted SHA-256 and 2FA.",
                    targetId = createdUser.id
                  )
                  onAuthenticated(createdUser)
                },
                onFailure = { err ->
                  errorMessage = err.message ?: "Configuration error"
                }
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("admin_initialize_button")
          ) {
            Text("INITIALIZE OWNER SECURITY & LOGIN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    } else if (isRecoveryMode) {
      // EMERGENCY RECOVERY MODE
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "Emergency Account Recovery",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumText
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Enter your secret emergency recovery passphrase to reset administrative credentials:",
            fontSize = 12.sp,
            color = SlateMuted
          )
          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = recoveryToken,
            onValueChange = { recoveryToken = it },
            label = { Text("Emergency Recovery Passphrase", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = AmberGold) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password (min 8 chars)", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberGold) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = newTwoFactorPin,
            onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) newTwoFactorPin = it },
            label = { Text("New 6-Digit 2FA PIN", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AmberGold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              errorMessage = null
              val res = securityManager.recoverAccount(recoveryToken, newPassword, newTwoFactorPin)
              res.fold(
                onSuccess = {
                  successMessage = "Credentials updated successfully. Please authenticate."
                  isRecoveryMode = false
                  recoveryToken = ""
                  newPassword = ""
                  newTwoFactorPin = ""
                },
                onFailure = { err ->
                  errorMessage = err.message ?: "Recovery failed."
                }
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
          ) {
            Text("RESET CREDENTIALS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }

          Spacer(modifier = Modifier.height(8.dp))

          TextButton(
            onClick = { isRecoveryMode = false },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Back to Authenticator", color = SlateMuted, fontSize = 12.sp)
          }
        }
      }
    } else {
      // NORMAL LOGIN MODE
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "Administrator Sign In",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumText
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Owner established credentials required for access to /admin.",
            fontSize = 12.sp,
            color = SlateMuted
          )
          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Administrator Email", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AmberGold) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_login_email"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberGold) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = null,
                  tint = SlateMuted
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_login_password"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = twoFactorPin,
            onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) twoFactorPin = it },
            label = { Text("Secondary Factor (6-Digit 2FA)", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AmberGold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_login_2fa_pin"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              errorMessage = null
              when (val result = securityManager.authenticate(email, password, twoFactorPin)) {
                is AdminAuthResult.Success -> {
                  viewModel.switchUser(result.user)
                  viewModel.recordAuditLog(
                    action = "ADMIN_LOGIN_SUCCESS",
                    details = "Administrator authenticated to /admin with valid salted credentials and 2FA.",
                    targetId = result.user.id
                  )
                  onAuthenticated(result.user)
                }
                is AdminAuthResult.Failure -> {
                  errorMessage = result.reason
                  viewModel.recordAuditLog(
                    action = "ADMIN_LOGIN_FAILED",
                    details = "Failed authentication attempt for email: $email. Reason: ${result.reason}",
                    targetId = email
                  )
                }
                is AdminAuthResult.NotConfigured -> {
                  isConfigured = false
                }
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("admin_login_button")
          ) {
            Text("AUTHENTICATE & ENTER CONTROL PANEL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextButton(onClick = { isRecoveryMode = true }) {
              Text("Emergency Recovery", color = AmberGold, fontSize = 12.sp)
            }

            TextButton(onClick = onExit) {
              Text("Return to Marketplace", color = SlateMuted, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}
