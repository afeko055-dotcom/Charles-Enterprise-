package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.VehicleEntity
import com.example.ui.components.VehicleCard
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
fun HomeScreen(
  viewModel: MarketplaceViewModel,
  onVehicleClick: (VehicleEntity) -> Unit,
  onNavigateToSearch: () -> Unit,
  modifier: Modifier = Modifier
) {
  val vehicles by viewModel.filteredVehicles.collectAsStateWithLifecycle()
  val savedVehicles by viewModel.savedVehicles.collectAsStateWithLifecycle()
  val savedIds = remember(savedVehicles) { savedVehicles.map { it.id }.toSet() }

  val context = LocalContext.current
  val heroImageRes = remember {
    val id = context.resources.getIdentifier("img_hero_car", "drawable", context.packageName)
    if (id != 0) id else 0
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("home_screen_list"),
    contentPadding = PaddingValues(bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Hero Showcase Section
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(280.dp)
      ) {
        if (heroImageRes != 0) {
          Image(
            painter = painterResource(id = heroImageRes),
            contentDescription = "Apex Motors Hero Vault",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF10192A))
          )
        }

        // Luxury Gradient Vignette
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color(0x99090D16),
                  Color(0x40090D16),
                  Color(0xFF090D16)
                )
              )
            )
        )

        // Hero Typography
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(18.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = AmberGold.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f))
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "100% ESCROW PROTECTED AUTOMOTIVE MARKETPLACE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AmberGold,
                letterSpacing = 1.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Automotive Prestige.\nSecured & Delivered.",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = PlatinumText,
            lineHeight = 32.sp
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Verified luxury vehicles with secure hosted checkout and enclosed carrier dispatch directly to your doorstep.",
            fontSize = 13.sp,
            color = SlateMuted
          )
        }
      }
    }

    // 2. Search & Quick Filters Bar
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = CarbonCard,
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToSearch() }
            .testTag("home_search_bar")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = AmberGold,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Search Mercedes-AMG, Porsche, Rolls-Royce, Range Rover...",
              fontSize = 13.sp,
              color = SlateMuted,
              maxLines = 1
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Brand Selector Pills
        val filterCriteria by viewModel.filterCriteria.collectAsStateWithLifecycle()
        val brands = listOf("All Brands", "Mercedes-AMG", "Porsche", "Rolls-Royce", "Land Rover", "BMW", "Ferrari")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          brands.forEach { brand ->
            val isSelected = if (brand == "All Brands") filterCriteria.brand == null else filterCriteria.brand == brand
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) AmberGold else CarbonCard,
              border = BorderStroke(1.dp, if (isSelected) AmberGold else CarbonBorder),
              modifier = Modifier
                .clickable {
                  val newBrand = if (brand == "All Brands") null else brand
                  viewModel.updateFilterCriteria { it.copy(brand = newBrand) }
                }
                .testTag("brand_pill_$brand")
            ) {
              Text(
                text = brand,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF090D16) else PlatinumText,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              )
            }
          }
        }
      }
    }

    // 3. How It Works (Trust & Security Feature Banner)
    item {
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
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = ElectricCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "THE APEX ASSURANCE PROTOCOL",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = ElectricCyan
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            AssuranceStep(number = "1", title = "Vault Inspection", desc = "150-Point Certified VIN Audit", icon = Icons.Default.VerifiedUser)
            AssuranceStep(number = "2", title = "Hosted Escrow", desc = "Zero credentials stored on site", icon = Icons.Default.Payment)
            AssuranceStep(number = "3", title = "OTP Handover", desc = "Funds released after inspection", icon = Icons.Default.LocalShipping)
          }
        }
      }
    }

    // 4. Section Header: Available Inventory
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Curated Automobile Vault",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumText
          )
          Text(
            text = "${vehicles.size} verified luxury vehicles ready for immediate delivery",
            fontSize = 12.sp,
            color = SlateMuted
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = CarbonCard,
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.clickable { onNavigateToSearch() }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = "Filter",
              fontSize = 12.sp,
              color = AmberGold,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = AmberGold,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }
    }

    // 5. Vehicles List
    items(vehicles, key = { it.id }) { vehicle ->
      Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        VehicleCard(
          vehicle = vehicle,
          isSaved = savedIds.contains(vehicle.id),
          onCardClick = { onVehicleClick(vehicle) },
          onSaveClick = { viewModel.toggleSaveVehicle(vehicle.id) }
        )
      }
    }
  }
}

@Composable
fun AssuranceStep(
  number: String,
  title: String,
  desc: String,
  icon: ImageVector
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(100.dp)
  ) {
    Surface(
      shape = CircleShape,
      color = Color(0xFF1B283E),
      border = BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier.size(42.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = AmberGold,
          modifier = Modifier.size(20.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = PlatinumText,
      maxLines = 1
    )
    Text(
      text = desc,
      fontSize = 9.sp,
      color = SlateMuted,
      lineHeight = 12.sp,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
  }
}
