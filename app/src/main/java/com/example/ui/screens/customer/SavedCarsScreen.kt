package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import com.example.viewmodel.MarketplaceViewModel

@Composable
fun SavedCarsScreen(
  viewModel: MarketplaceViewModel,
  onVehicleClick: (VehicleEntity) -> Unit,
  onExploreClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val savedCars by viewModel.savedVehicles.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("saved_cars_screen")
  ) {
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
        Text(
          text = "Saved Vault Vehicles",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = PlatinumText
        )
        Text(
          text = "Your curated wishlist of luxury automobiles",
          fontSize = 11.sp,
          color = SlateMuted
        )
      }
    }

    if (savedCars.isEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = CarbonCard,
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.size(72.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.BookmarkBorder,
              contentDescription = null,
              tint = SlateMuted,
              modifier = Modifier.size(36.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "No Saved Vehicles",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = PlatinumText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Bookmark vehicles from the showroom to compare specifications and monitor pricing.",
          fontSize = 12.sp,
          color = SlateMuted,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onExploreClick,
          colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16))
        ) {
          Text("Browse Vault", fontWeight = FontWeight.Bold)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(savedCars, key = { it.id }) { vehicle ->
          VehicleCard(
            vehicle = vehicle,
            isSaved = true,
            onCardClick = { onVehicleClick(vehicle) },
            onSaveClick = { viewModel.toggleSaveVehicle(vehicle.id) }
          )
        }
      }
    }
  }
}
