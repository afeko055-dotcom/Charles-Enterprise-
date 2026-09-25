package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CurrencyFormatter
import com.example.data.model.PaymentGateway
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import com.example.viewmodel.CheckoutState
import com.example.viewmodel.MarketplaceViewModel

@Composable
fun CheckoutHostedScreen(
  viewModel: MarketplaceViewModel,
  onNavigateToTracking: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier
) {
  val checkoutState by viewModel.checkoutState.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  var selectedPaymentTab by remember { mutableIntStateOf(0) } // 0: Card, 1: Bank Transfer, 2: USSD

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("checkout_hosted_screen")
  ) {
    when (val state = checkoutState) {
      is CheckoutState.Loading -> {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CircularProgressIndicator(color = AmberGold)
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Connecting to Secure Gateway...",
            fontSize = 15.sp,
            color = PlatinumText,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "Securing cryptographic channel & verifying inventory lock",
            fontSize = 12.sp,
            color = SlateMuted
          )
        }
      }

      is CheckoutState.Error -> {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = CrimsonAlert,
            modifier = Modifier.size(54.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Checkout Failed",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumText
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = state.message,
            fontSize = 13.sp,
            color = SlateMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = {
              viewModel.resetCheckout()
              onCancel()
            },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16))
          ) {
            Text("Return to Vault")
          }
        }
      }

      is CheckoutState.Success -> {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = CircleShape,
            color = EmeraldSuccess.copy(alpha = 0.2f),
            border = BorderStroke(2.dp, EmeraldSuccess),
            modifier = Modifier.size(72.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldSuccess,
                modifier = Modifier.size(40.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(18.dp))
          Text(
            text = "Payment Verified Successfully!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = PlatinumText
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Order ID: ${state.orderId} • Ref: ${state.reference}",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = AmberGold
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Your vehicle has been marked SOLD and dedicated flatbed transport is dispatched. Track your delivery in real-time.",
            fontSize = 13.sp,
            color = SlateMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(modifier = Modifier.height(28.dp))
          Button(
            onClick = { onNavigateToTracking() },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("track_delivery_now_button")
          ) {
            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Track Live Carrier Delivery", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
        }
      }

      is CheckoutState.HostedCheckoutReady -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Top Provider Banner
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1E36)),
              border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Security,
                      contentDescription = null,
                      tint = ElectricCyan,
                      modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = state.gateway.displayName.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ElectricCyan
                      )
                      Text(
                        text = "SECURE EXTERNAL HOSTED CHECKOUT",
                        fontSize = 10.sp,
                        color = SlateMuted,
                        letterSpacing = 1.sp
                      )
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldSuccess.copy(alpha = 0.2f)
                  ) {
                    Text(
                      text = "256-BIT SSL",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldSuccess,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "IMPORTANT SECURITY MANDATE: Apex Motors never collects, asks for, or stores your banking credentials, passwords, or card PINs. You are transacting directly on the official ${state.gateway.displayName.split(" ").first()} hosted infrastructure.",
                  fontSize = 11.sp,
                  color = SlateMuted,
                  lineHeight = 16.sp
                )
              }
            }
          }

          // Hosted Checkout Details Card
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = CarbonCard),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "Transaction Details",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = PlatinumText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Client:", fontSize = 12.sp, color = SlateMuted)
                  Text("${currentUser.fullName} (${currentUser.email})", fontSize = 12.sp, color = PlatinumText, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Gateway Reference:", fontSize = 12.sp, color = SlateMuted)
                  Text(state.reference, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = AmberGold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Hosted Checkout URL:", fontSize = 12.sp, color = SlateMuted)
                  Text(state.checkoutUrl, fontSize = 10.sp, color = ElectricCyan, maxLines = 1)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Tabs on Gateway
                TabRow(
                  selectedTabIndex = selectedPaymentTab,
                  containerColor = Color(0xFF090D16),
                  contentColor = AmberGold,
                  indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                      Modifier.tabIndicatorOffset(tabPositions[selectedPaymentTab]),
                      color = AmberGold
                    )
                  }
                ) {
                  Tab(
                    selected = selectedPaymentTab == 0,
                    onClick = { selectedPaymentTab = 0 },
                    text = { Text("Debit/Credit Card", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                  )
                  Tab(
                    selected = selectedPaymentTab == 1,
                    onClick = { selectedPaymentTab = 1 },
                    text = { Text("Bank Transfer", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                  )
                  Tab(
                    selected = selectedPaymentTab == 2,
                    onClick = { selectedPaymentTab = 2 },
                    text = { Text("USSD", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedPaymentTab) {
                  0 -> {
                    // Card Method Tab
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090D16), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3D-Secure 2.0 Card Payment", fontWeight = FontWeight.Bold, color = PlatinumText, fontSize = 13.sp)
                      }
                      Spacer(modifier = Modifier.height(8.dp))
                      Text("Supported: Mastercard, Visa, Verve. Tokenized by ${state.gateway.displayName.split(" ").first()}.", color = SlateMuted, fontSize = 11.sp)
                    }
                  }
                  1 -> {
                    // Virtual Dedicated NUBAN Account Tab
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090D16), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dedicated Virtual Escrow Account", fontWeight = FontWeight.Bold, color = PlatinumText, fontSize = 13.sp)
                      }
                      Spacer(modifier = Modifier.height(8.dp))
                      Text("Bank: Wema Bank / Titan Trust", color = SlateMuted, fontSize = 11.sp)
                      Text("Account Number: 9948201948", color = AmberGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                      Text("Account Name: APEX MOTORS ESCROW", color = PlatinumText, fontSize = 11.sp)
                    }
                  }
                  2 -> {
                    // USSD Tab
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090D16), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                    ) {
                      Text("Instant Mobile USSD String", fontWeight = FontWeight.Bold, color = PlatinumText, fontSize = 13.sp)
                      Spacer(modifier = Modifier.height(6.dp))
                      Text("*737*50*AMOUNT*1829# (GTBank)", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                      Text("Or dial *966*000*819# (Zenith Bank)", color = SlateMuted, fontSize = 11.sp)
                    }
                  }
                }
              }
            }
          }

          // Test & Verification Actions
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "VERIFIED SETTLEMENT GATEWAY ACTIONS",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = AmberGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Notice: The customer's redirect is never considered proof of payment. The backend strictly contacts the provider to verify amount, currency, and reference status before releasing inventory.",
                  fontSize = 11.sp,
                  color = SlateMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Action: Simulate Verified Completion
                Button(
                  onClick = { viewModel.completeHostedPayment(state.reference, state.gateway) },
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("simulate_payment_success_button")
                ) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Confirm Payment via Server API Verification", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action: Cancel
                OutlinedButton(
                  onClick = {
                    viewModel.resetCheckout()
                    onCancel()
                  },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateMuted),
                  border = BorderStroke(1.dp, CarbonBorder),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cancel_checkout_button")
                ) {
                  Text("Cancel & Release Reservation", fontSize = 12.sp)
                }
              }
            }
          }
        }
      }

      else -> {
        // Idle
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text("No active checkout session.", color = SlateMuted)
          Spacer(modifier = Modifier.height(12.dp))
          Button(onClick = onCancel) { Text("Back to Showroom") }
        }
      }
    }
  }
}
