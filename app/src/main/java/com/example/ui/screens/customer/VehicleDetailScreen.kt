package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.VehicleEntity
import com.example.data.model.CurrencyFormatter
import com.example.data.model.PaymentGateway
import com.example.data.model.VehicleStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import com.example.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
  vehicle: VehicleEntity,
  viewModel: MarketplaceViewModel,
  onBack: () -> Unit,
  onProceedToHostedCheckout: () -> Unit,
  modifier: Modifier = Modifier
) {
  val savedVehicles by viewModel.savedVehicles.collectAsStateWithLifecycle()
  val isSaved = remember(savedVehicles) { savedVehicles.any { it.id == vehicle.id } }

  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  var showCheckoutModal by remember { mutableStateOf(false) }
  var deliveryAddress by remember { mutableStateOf(currentUser.address) }
  var deliveryCity by remember { mutableStateOf("Lagos") }
  var deliveryState by remember { mutableStateOf("Lagos State") }
  var contactPhone by remember { mutableStateOf(currentUser.phone) }
  var selectedGateway by remember { mutableStateOf(PaymentGateway.PAYSTACK) }

  val context = LocalContext.current
  val imageResId = remember(vehicle.imageResName) {
    val id = context.resources.getIdentifier(vehicle.imageResName, "drawable", context.packageName)
    if (id != 0) id else context.resources.getIdentifier("img_hero_car", "drawable", context.packageName)
  }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("vehicle_detail_scroll"),
      contentPadding = PaddingValues(bottom = 110.dp)
    ) {
      // 1. Full-Bleed Image Gallery Hero
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
        ) {
          if (imageResId != 0) {
            Image(
              painter = painterResource(id = imageResId),
              contentDescription = "${vehicle.brand} ${vehicle.model}",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color(0x99090D16),
                    Color.Transparent,
                    Color(0xBB090D16),
                    Color(0xFF090D16)
                  )
                )
              )
          )

          // Navigation Top Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = Color.Black.copy(alpha = 0.6f),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier
                .size(42.dp)
                .clickable { onBack() }
                .testTag("vehicle_detail_back_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  tint = PlatinumText,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            Surface(
              shape = CircleShape,
              color = Color.Black.copy(alpha = 0.6f),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier
                .size(42.dp)
                .clickable { viewModel.toggleSaveVehicle(vehicle.id) }
                .testTag("vehicle_detail_save_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                  contentDescription = "Save",
                  tint = if (isSaved) AmberGold else PlatinumText,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          // Status & VIN badge bottom
          Row(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            StatusBadge(
              text = vehicle.status.label.uppercase(),
              color = when (vehicle.status) {
                VehicleStatus.AVAILABLE -> EmeraldSuccess
                VehicleStatus.RESERVED -> AmberGold
                VehicleStatus.SOLD -> CrimsonAlert
                else -> ElectricCyan
              }
            )

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.Black.copy(alpha = 0.7f),
              border = BorderStroke(1.dp, CarbonBorder)
            ) {
              Text(
                text = "VIN: ${vehicle.vin}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateMuted,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // 2. Title & Authoritative Price Block
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
          Text(
            text = "${vehicle.year} ${vehicle.brand} ${vehicle.model}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = PlatinumText
          )

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Text(
                text = CurrencyFormatter.formatKoboToNaira(vehicle.priceKobo),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = AmberGold
              )
              Text(
                text = "Authoritative Price • Approx. ${CurrencyFormatter.formatKoboToUSD(vehicle.priceKobo)} USD",
                fontSize = 12.sp,
                color = SlateMuted
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = CarbonCard,
              border = BorderStroke(1.dp, CarbonBorder)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = AmberGold,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = vehicle.location,
                  fontSize = 11.sp,
                  color = PlatinumText,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }

      // 3. Technical Performance Metrics Grid
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
          Text(
            text = "PERFORMANCE & SPECIFICATIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = SlateMuted
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            MetricCard(label = "Horsepower", value = "${vehicle.horsepower} HP", sub = "Output", modifier = Modifier.weight(1f))
            MetricCard(label = "0-100 KM/H", value = "${vehicle.acceleration0to100}s", sub = "Acceleration", modifier = Modifier.weight(1f))
            MetricCard(label = "Mileage", value = "${vehicle.mileageKm} km", sub = "Verified Odo", modifier = Modifier.weight(1f))
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            MetricCard(label = "Transmission", value = vehicle.transmission.label, sub = "Gearbox", modifier = Modifier.weight(1f))
            MetricCard(label = "Fuel / Powertrain", value = vehicle.fuelType.label.split(" ").first(), sub = "Fuel Type", modifier = Modifier.weight(1f))
            MetricCard(label = "Condition", value = vehicle.condition.label.split(" ").first(), sub = "History", modifier = Modifier.weight(1f))
          }
        }
      }

      // 4. Description & Features
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CarbonCard),
          border = BorderStroke(1.dp, CarbonBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "VEHICLE OVERVIEW",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = AmberGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = vehicle.description,
              fontSize = 13.sp,
              color = PlatinumText,
              lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "Engine Specs: ${vehicle.engineSpecs}",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = ElectricCyan
            )
            Text(
              text = "Color: ${vehicle.color} • Body Style: ${vehicle.bodyType.label}",
              fontSize = 12.sp,
              color = SlateMuted
            )
          }
        }
      }

      // 5. 150-Point Certified Inspection Report
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A2E)),
          border = BorderStroke(1.dp, Color(0xFF1E3A5F))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = EmeraldSuccess,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "150-POINT VAULT INSPECTION: CERTIFIED",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = EmeraldSuccess
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            InspectionItem("Engine Compression & OBD-II Diagnostics", "Passed • Zero Fault Codes")
            InspectionItem("Chassis & Frame Structural Integrity", "Certified Uncompromised")
            InspectionItem("Braking & High-Performance Suspension", "100% Caliper & Pad Thickness")
            InspectionItem("Customs & Documentation Verification", "Full FIRS & Nigeria Customs Duty Paid")
          }
        }
      }
    }

    // Fixed Bottom Action Bar: Reserve & Checkout
    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth(),
      color = Color(0xF2090D16),
      border = BorderStroke(1.dp, CarbonBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Total Vault Price",
            fontSize = 11.sp,
            color = SlateMuted
          )
          Text(
            text = CurrencyFormatter.formatKoboToNaira(vehicle.priceKobo),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = AmberGold
          )
        }

        Button(
          onClick = { showCheckoutModal = true },
          enabled = vehicle.status == VehicleStatus.AVAILABLE,
          colors = ButtonDefaults.buttonColors(
            containerColor = AmberGold,
            contentColor = Color(0xFF090D16),
            disabledContainerColor = Color(0xFF1B283E),
            disabledContentColor = SlateMuted
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .height(50.dp)
            .testTag("reserve_pay_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (vehicle.status == VehicleStatus.AVAILABLE) "Reserve & Pay" else vehicle.status.label,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Modal Bottom Sheet: Order Confirmation & Delivery Details
    if (showCheckoutModal) {
      ModalBottomSheet(
        onDismissRequest = { showCheckoutModal = false },
        sheetState = sheetState,
        containerColor = CarbonCard,
        dragHandle = {
          Box(
            modifier = Modifier
              .padding(vertical = 10.dp)
              .size(width = 40.dp, height = 4.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(SlateMuted)
          )
        }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
          Text(
            text = "Order Initiation & Delivery Dispatch",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumText
          )
          Text(
            text = "Authoritative vehicle reservation on database ledger.",
            fontSize = 12.sp,
            color = SlateMuted
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Breakdown Card
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Base Vehicle Price", color = SlateMuted, fontSize = 12.sp)
                Text(CurrencyFormatter.formatKoboToNaira(vehicle.priceKobo), color = PlatinumText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Enclosed Flatbed Carrier Delivery", color = SlateMuted, fontSize = 12.sp)
                Text("₦450,000", color = PlatinumText, fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Pre-Delivery Inspection & Documentation", color = SlateMuted, fontSize = 12.sp)
                Text("₦250,000", color = PlatinumText, fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CarbonBorder))
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Authoritative Total Amount", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                val totalKobo = vehicle.priceKobo + 450_000_00L + 250_000_00L
                Text(CurrencyFormatter.formatKoboToNaira(totalKobo), color = AmberGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Delivery Inputs
          OutlinedTextField(
            value = deliveryAddress,
            onValueChange = { deliveryAddress = it },
            label = { Text("Delivery Residence / Office Address") },
            modifier = Modifier.fillMaxWidth().testTag("checkout_address_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = PlatinumText,
              unfocusedTextColor = PlatinumText,
              focusedBorderColor = AmberGold,
              unfocusedBorderColor = CarbonBorder
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = deliveryCity,
              onValueChange = { deliveryCity = it },
              label = { Text("City") },
              modifier = Modifier.weight(1f),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PlatinumText,
                unfocusedTextColor = PlatinumText,
                focusedBorderColor = AmberGold,
                unfocusedBorderColor = CarbonBorder
              )
            )

            OutlinedTextField(
              value = contactPhone,
              onValueChange = { contactPhone = it },
              label = { Text("Driver Contact Phone") },
              modifier = Modifier.weight(1f).testTag("checkout_phone_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PlatinumText,
                unfocusedTextColor = PlatinumText,
                focusedBorderColor = AmberGold,
                unfocusedBorderColor = CarbonBorder
              )
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Hosted Provider Selector
          Text(
            text = "SELECT SECURE HOSTED PAYMENT GATEWAY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SlateMuted
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            GatewayChoiceCard(
              gateway = PaymentGateway.PAYSTACK,
              isSelected = selectedGateway == PaymentGateway.PAYSTACK,
              onSelect = { selectedGateway = PaymentGateway.PAYSTACK },
              modifier = Modifier.weight(1f)
            )

            GatewayChoiceCard(
              gateway = PaymentGateway.FLUTTERWAVE,
              isSelected = selectedGateway == PaymentGateway.FLUTTERWAVE,
              onSelect = { selectedGateway = PaymentGateway.FLUTTERWAVE },
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Button(
            onClick = {
              showCheckoutModal = false
              viewModel.startCheckout(
                vehicle = vehicle,
                deliveryAddress = deliveryAddress,
                deliveryCity = deliveryCity,
                deliveryState = deliveryState,
                contactPhone = contactPhone,
                gateway = selectedGateway
              )
              onProceedToHostedCheckout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("confirm_proceed_to_gateway_button")
          ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Proceed to Hosted ${selectedGateway.displayName.split(" ").first()}",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}

@Composable
fun GatewayChoiceCard(
  gateway: PaymentGateway,
  isSelected: Boolean,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clickable { onSelect() }
      .testTag("gateway_choice_${gateway.name}"),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) Color(0xFF1B283E) else Color(0xFF090D16)
    ),
    border = BorderStroke(1.dp, if (isSelected) AmberGold else CarbonBorder)
  ) {
    Row(
      modifier = Modifier.padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      RadioButton(
        selected = isSelected,
        onClick = { onSelect() },
        colors = RadioButtonDefaults.colors(selectedColor = AmberGold)
      )
      Column {
        Text(
          text = gateway.displayName.split(" ").first(),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = PlatinumText
        )
        Text(
          text = "Hosted Checkout",
          fontSize = 10.sp,
          color = SlateMuted
        )
      }
    }
  }
}

@Composable
fun MetricCard(
  label: String,
  value: String,
  sub: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = CarbonCard),
    border = BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(label, fontSize = 10.sp, color = SlateMuted, maxLines = 1)
      Spacer(modifier = Modifier.height(4.dp))
      Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PlatinumText, maxLines = 1)
      Text(sub, fontSize = 9.sp, color = AmberGold)
    }
  }
}

@Composable
fun InspectionItem(title: String, status: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = EmeraldSuccess,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(text = title, fontSize = 11.sp, color = PlatinumText)
    }
    Text(text = status, fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
  }
}
