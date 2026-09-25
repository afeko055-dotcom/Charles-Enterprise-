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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.data.entity.OrderEntity
import com.example.data.model.DeliveryStatus
import com.example.ui.components.LiveGpsRouteMapCanvas
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
fun LiveTrackingScreen(
  order: OrderEntity,
  viewModel: MarketplaceViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val delivery by viewModel.trackingDelivery.collectAsStateWithLifecycle()

  // Calculate route fraction based on status & remaining distance
  val progressFraction = remember(delivery?.status, delivery?.distanceRemainingKm) {
    when (delivery?.status) {
      DeliveryStatus.ASSIGNED, DeliveryStatus.ACCEPTED -> 0.08f
      DeliveryStatus.PICKUP_PENDING -> 0.18f
      DeliveryStatus.PICKED_UP -> 0.32f
      DeliveryStatus.IN_TRANSIT -> {
        val remaining = delivery?.distanceRemainingKm ?: 10.0
        (1.0f - (remaining / 18.0f).toFloat()).coerceIn(0.35f, 0.85f)
      }
      DeliveryStatus.NEAR_DESTINATION -> 0.92f
      DeliveryStatus.DELIVERED -> 1.0f
      else -> 0.1f
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("live_tracking_screen")
  ) {
    // Header Bar
    Surface(
      color = Color(0xF2090D16),
      border = BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp, bottom = 14.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = CarbonCard,
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier
            .size(38.dp)
            .clickable { onBack() }
            .testTag("tracking_back_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = PlatinumText,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
          Text(
            text = "Live Dispatch Tracking",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = PlatinumText
          )
          Text(
            text = "Order #${order.orderNumber} • ${order.vehicleTitle}",
            fontSize = 11.sp,
            color = SlateMuted
          )
        }
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Live Animated GPS Radar & Highway Route Canvas
      item {
        LiveGpsRouteMapCanvas(
          progressFraction = progressFraction,
          carrierHeading = delivery?.currentHeading ?: 45f,
          speedKmh = delivery?.currentSpeedKmh ?: 58f
        )
      }

      // 2. Secret Delivery OTP Card (Crucial for Handover Confirmation)
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A0F)),
          border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_otp_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Key,
                  contentDescription = null,
                  tint = AmberGold,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "DELIVERY CONFIRMATION OTP",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = AmberGold
                )
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = AmberGold.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "CONFIDENTIAL",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = AmberGold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6-digit OTP Display
            val plainOtp = delivery?.deliveryOtpPlainForCustomer ?: "839104"
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF090D16),
              border = BorderStroke(1.dp, AmberGold),
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 14.dp)
              ) {
                Text(
                  text = plainOtp.chunked(3).joinToString("  "),
                  fontSize = 32.sp,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 6.sp,
                  color = AmberGold
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "DO NOT SHARE THIS CODE OVER THE PHONE. Only disclose this 6-digit OTP to the carrier driver after inspecting your ${order.vehicleTitle} at your doorstep. Entering this OTP authorizes vehicle handover.",
              fontSize = 11.sp,
              color = SlateMuted,
              lineHeight = 16.sp
            )
          }
        }
      }

      // 3. Dispatch Status & ETA Telemetry
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CarbonCard),
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Dispatch Telemetry",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PlatinumText
              )
              StatusBadge(
                text = (delivery?.status ?: DeliveryStatus.ASSIGNED).label,
                color = when (delivery?.status) {
                  DeliveryStatus.DELIVERED -> EmeraldSuccess
                  DeliveryStatus.IN_TRANSIT, DeliveryStatus.NEAR_DESTINATION -> ElectricCyan
                  else -> AmberGold
                }
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              TelemetryTile(label = "Estimated Arrival", value = "${delivery?.etaMinutes ?: 35} mins", modifier = Modifier.weight(1f))
              TelemetryTile(label = "Distance Remaining", value = "${String.format("%.1f", delivery?.distanceRemainingKm ?: 14.2)} km", modifier = Modifier.weight(1f))
              TelemetryTile(label = "Carrier Speed", value = "${delivery?.currentSpeedKmh?.toInt() ?: 55} km/h", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Carrier Unit Info
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090D16), RoundedCornerShape(12.dp))
                .padding(12.dp)
            ) {
              Surface(
                shape = CircleShape,
                color = Color(0xFF161F30),
                border = BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier.size(44.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = delivery?.riderName ?: "Chinedu Eze",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = PlatinumText
                )
                Text(
                  text = "${delivery?.carrierVehicle ?: "Mercedes Actros 3340 Flatbed"} • ${delivery?.carrierPlate ?: "APP-402-XA"}",
                  fontSize = 11.sp,
                  color = SlateMuted
                )
              }

              Surface(
                shape = CircleShape,
                color = EmeraldSuccess.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                modifier = Modifier
                  .size(38.dp)
                  .clickable {
                    viewModel.showMessage("Connecting call to carrier driver ${delivery?.riderName} (${delivery?.riderPhone})...")
                  }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call Driver",
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 4. Addresses
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CarbonCard),
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("LOGISTICS TRANSIT ROUTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateMuted)
            Spacer(modifier = Modifier.height(10.dp))
            AddressRow(label = "Pickup Hub", address = delivery?.pickupHubAddress ?: "Apex Central Showroom & Logistics Hub, Lekki Expressway, Lagos", isStart = true)
            Spacer(modifier = Modifier.height(10.dp))
            AddressRow(label = "Delivery Destination", address = "${order.deliveryAddress}, ${order.deliveryCity}, ${order.deliveryState}", isStart = false)
          }
        }
      }
    }
  }
}

@Composable
fun TelemetryTile(label: String, value: String, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
    border = BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(label, fontSize = 9.sp, color = SlateMuted, maxLines = 1)
      Spacer(modifier = Modifier.height(4.dp))
      Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = AmberGold, maxLines = 1)
    }
  }
}

@Composable
fun AddressRow(label: String, address: String, isStart: Boolean) {
  Row(verticalAlignment = Alignment.Top) {
    Icon(
      imageVector = if (isStart) Icons.Default.DirectionsCar else Icons.Default.LocationOn,
      contentDescription = null,
      tint = if (isStart) AmberGold else EmeraldSuccess,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateMuted)
      Text(text = address, fontSize = 12.sp, color = PlatinumText)
    }
  }
}
