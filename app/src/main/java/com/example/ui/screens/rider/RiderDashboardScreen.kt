package com.example.ui.screens.rider

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.DeliveryEntity
import com.example.data.model.DeliveryStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import com.example.viewmodel.MarketplaceViewModel

@Composable
fun RiderDashboardScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  val deliveries by viewModel.allDeliveries.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  var selectedDeliveryForOtp by remember { mutableStateOf<DeliveryEntity?>(null) }
  var enteredOtp by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("rider_dashboard_screen")
  ) {
    // Rider Header Bar
    Surface(
      color = Color(0xF2090D16),
      border = BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp, bottom = 14.dp, start = 16.dp, end = 16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = AmberGold.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, AmberGold),
              modifier = Modifier.size(40.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.LocalShipping,
                  contentDescription = null,
                  tint = AmberGold,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Dispatch Terminal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = PlatinumText
              )
              Text(
                text = "Flatbed Unit #4 • ${currentUser.fullName}",
                fontSize = 11.sp,
                color = SlateMuted
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldSuccess.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
          ) {
            Text(
              text = "CARRIER ON-DUTY",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldSuccess,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    if (deliveries.isEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Default.LocalShipping,
          contentDescription = null,
          tint = SlateMuted,
          modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "No Active Deliveries",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = PlatinumText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "All customer vehicle dispatches will appear here upon verified payment confirmation.",
          fontSize = 12.sp,
          color = SlateMuted,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(deliveries, key = { it.id }) { delivery ->
          RiderDeliveryCard(
            delivery = delivery,
            onUpdateStatus = { newStatus -> viewModel.updateDeliveryStatus(delivery.id, newStatus) },
            onBroadcastGps = { viewModel.simulateGpsMovement(delivery) },
            onVerifyOtpClick = {
              selectedDeliveryForOtp = delivery
              enteredOtp = ""
            }
          )
        }
      }
    }

    // OTP Handover Confirmation Dialog
    if (selectedDeliveryForOtp != null) {
      val delivery = selectedDeliveryForOtp!!
      AlertDialog(
        onDismissRequest = { selectedDeliveryForOtp = null },
        containerColor = CarbonCard,
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Key, contentDescription = null, tint = AmberGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verify Delivery Handover OTP", color = PlatinumText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }
        },
        text = {
          Column {
            Text(
              text = "Ask the customer for their 6-digit confirmation code shown on their tracking dashboard.",
              fontSize = 12.sp,
              color = SlateMuted
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = enteredOtp,
              onValueChange = { if (it.length <= 6) enteredOtp = it },
              placeholder = { Text("6-Digit OTP (e.g. ${delivery.deliveryOtpPlainForCustomer})") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("rider_otp_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PlatinumText,
                unfocusedTextColor = PlatinumText,
                focusedBorderColor = AmberGold,
                unfocusedBorderColor = CarbonBorder
              )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Hint for verification test: Customer OTP is ${delivery.deliveryOtpPlainForCustomer}",
              fontSize = 11.sp,
              color = AmberGold
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.verifyDeliveryOtp(delivery.id, enteredOtp)
              selectedDeliveryForOtp = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
            modifier = Modifier.testTag("submit_rider_otp_button")
          ) {
            Text("Authorize Handover")
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { selectedDeliveryForOtp = null }) {
            Text("Cancel", color = SlateMuted)
          }
        }
      )
    }
  }
}

@Composable
fun RiderDeliveryCard(
  delivery: DeliveryEntity,
  onUpdateStatus: (DeliveryStatus) -> Unit,
  onBroadcastGps: () -> Unit,
  onVerifyOtpClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CarbonCard),
    border = BorderStroke(1.dp, CarbonBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("rider_delivery_card_${delivery.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Delivery #${delivery.id.take(8).uppercase()}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AmberGold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Order ID: ${delivery.orderId}",
            fontSize = 11.sp,
            color = SlateMuted
          )
        }

        StatusBadge(
          text = delivery.status.label,
          color = when (delivery.status) {
            DeliveryStatus.DELIVERED -> EmeraldSuccess
            DeliveryStatus.IN_TRANSIT, DeliveryStatus.NEAR_DESTINATION -> ElectricCyan
            else -> AmberGold
          }
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Addresses
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF090D16), RoundedCornerShape(10.dp))
          .padding(12.dp)
      ) {
        Row(verticalAlignment = Alignment.Top) {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text("Pickup Hub:", fontSize = 10.sp, color = SlateMuted, fontWeight = FontWeight.Bold)
            Text(delivery.pickupHubAddress, fontSize = 11.sp, color = PlatinumText)
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Top) {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text("Destination Customer Residence:", fontSize = 10.sp, color = SlateMuted, fontWeight = FontWeight.Bold)
            Text(delivery.destinationAddress, fontSize = 11.sp, color = PlatinumText)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Telemetry row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Distance: ${String.format("%.1f", delivery.distanceRemainingKm)} km", fontSize = 12.sp, color = SlateMuted)
        Text("ETA: ${delivery.etaMinutes} mins", fontSize = 12.sp, color = AmberGold, fontWeight = FontWeight.Bold)
        Text("Speed: ${delivery.currentSpeedKmh.toInt()} km/h", fontSize = 12.sp, color = ElectricCyan)
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action workflow buttons according to status
      when (delivery.status) {
        DeliveryStatus.ASSIGNED -> {
          Button(
            onClick = { onUpdateStatus(DeliveryStatus.ACCEPTED) },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16)),
            modifier = Modifier.fillMaxWidth().testTag("rider_accept_button")
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Accept Delivery Dispatch", fontWeight = FontWeight.Bold)
          }
        }

        DeliveryStatus.ACCEPTED -> {
          Button(
            onClick = { onUpdateStatus(DeliveryStatus.PICKUP_PENDING) },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF00253B)),
            modifier = Modifier.fillMaxWidth().testTag("rider_start_route_button")
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Start Route to Showroom Hub", fontWeight = FontWeight.Bold)
          }
        }

        DeliveryStatus.PICKUP_PENDING -> {
          Button(
            onClick = { onUpdateStatus(DeliveryStatus.PICKED_UP) },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16)),
            modifier = Modifier.fillMaxWidth().testTag("rider_secured_button")
          ) {
            Text("Vehicle Inspected & Secured on Flatbed", fontWeight = FontWeight.Bold)
          }
        }

        DeliveryStatus.PICKED_UP -> {
          Button(
            onClick = { onUpdateStatus(DeliveryStatus.IN_TRANSIT) },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF00253B)),
            modifier = Modifier.fillMaxWidth().testTag("rider_start_transit_button")
          ) {
            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Begin Highway Transit", fontWeight = FontWeight.Bold)
          }
        }

        DeliveryStatus.IN_TRANSIT -> {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = onBroadcastGps,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F), contentColor = ElectricCyan),
              border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth().testTag("broadcast_gps_button")
            ) {
              Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Broadcast Live GPS Ping (Advance On Route)", fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { onUpdateStatus(DeliveryStatus.NEAR_DESTINATION) },
              colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16)),
              modifier = Modifier.fillMaxWidth().testTag("arrived_vicinity_button")
            ) {
              Text("Arrived in Customer Neighborhood", fontWeight = FontWeight.Bold)
            }
          }
        }

        DeliveryStatus.NEAR_DESTINATION -> {
          Button(
            onClick = onVerifyOtpClick,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("rider_enter_otp_button")
          ) {
            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enter Customer OTP to Confirm Handover", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }

        DeliveryStatus.DELIVERED -> {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldSuccess.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Vehicle Delivered & OTP Verified", fontWeight = FontWeight.Bold, color = EmeraldSuccess, fontSize = 12.sp)
            }
          }
        }

        else -> {}
      }
    }
  }
}
