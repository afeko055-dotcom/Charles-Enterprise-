package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.VehicleEntity
import com.example.data.model.BodyType
import com.example.data.model.FuelType
import com.example.data.model.TransmissionType
import com.example.data.model.VehicleCondition
import com.example.ui.components.VehicleCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.SortOption

@Composable
fun SearchFilterScreen(
  viewModel: MarketplaceViewModel,
  onVehicleClick: (VehicleEntity) -> Unit,
  onNavigateToAdmin: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val vehicles by viewModel.filteredVehicles.collectAsStateWithLifecycle()
  val savedVehicles by viewModel.savedVehicles.collectAsStateWithLifecycle()
  val savedIds = remember(savedVehicles) { savedVehicles.map { it.id }.toSet() }

  val filterCriteria by viewModel.filterCriteria.collectAsStateWithLifecycle()
  val query = filterCriteria.query
  val selectedBody = filterCriteria.bodyType
  val selectedTrans = filterCriteria.transmission
  val selectedFuel = filterCriteria.fuelType
  val selectedCond = filterCriteria.condition
  val sortOpt = filterCriteria.sort

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("search_filter_screen")
  ) {
    // Top Search Input
    Surface(
      color = Color(0xF2090D16),
      border = BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
      ) {
        OutlinedTextField(
          value = query,
          onValueChange = { newQ ->
            viewModel.updateFilterCriteria { it.copy(query = newQ) }
            if (newQ.trim() == "/admin") {
              viewModel.updateFilterCriteria { it.copy(query = "") }
              onNavigateToAdmin?.invoke()
            }
          },
          placeholder = { Text("Search brand, model, VIN, location...", fontSize = 13.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AmberGold)
          },
          trailingIcon = {
            if (query.isNotEmpty()) {
              IconButton(onClick = { viewModel.updateFilterCriteria { it.copy(query = "") } }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = SlateMuted)
              }
            }
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PlatinumText,
            unfocusedTextColor = PlatinumText,
            focusedContainerColor = CarbonCard,
            unfocusedContainerColor = CarbonCard,
            focusedBorderColor = AmberGold,
            unfocusedBorderColor = CarbonBorder
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_input_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Filters Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Sort Pill
          FilterPill(
            label = "Sort: ${sortOpt.label.split(" ").take(2).joinToString(" ")}",
            isActive = sortOpt != SortOption.FEATURED,
            onClick = {
              val all = SortOption.values()
              val nextIdx = (all.indexOf(sortOpt) + 1) % all.size
              viewModel.updateFilterCriteria { it.copy(sort = all[nextIdx]) }
            }
          )

          // Body Type Pill
          FilterPill(
            label = if (selectedBody != null) selectedBody!!.label else "Body Type",
            isActive = selectedBody != null,
            onClick = {
              val all = listOf(null) + BodyType.values()
              val next = (all.indexOf(selectedBody) + 1) % all.size
              viewModel.updateFilterCriteria { it.copy(bodyType = all[next]) }
            }
          )

          // Transmission Pill
          FilterPill(
            label = if (selectedTrans != null) selectedTrans!!.label else "Gearbox",
            isActive = selectedTrans != null,
            onClick = {
              val all = listOf(null) + TransmissionType.values()
              val next = (all.indexOf(selectedTrans) + 1) % all.size
              viewModel.updateFilterCriteria { it.copy(transmission = all[next]) }
            }
          )

          // Fuel Pill
          FilterPill(
            label = if (selectedFuel != null) selectedFuel!!.label.split(" ").first() else "Fuel",
            isActive = selectedFuel != null,
            onClick = {
              val all = listOf(null) + FuelType.values()
              val next = (all.indexOf(selectedFuel) + 1) % all.size
              viewModel.updateFilterCriteria { it.copy(fuelType = all[next]) }
            }
          )

          // Condition Pill
          FilterPill(
            label = if (selectedCond != null) selectedCond!!.label.split(" ").first() else "Condition",
            isActive = selectedCond != null,
            onClick = {
              val all = listOf(null) + VehicleCondition.values()
              val next = (all.indexOf(selectedCond) + 1) % all.size
              viewModel.updateFilterCriteria { it.copy(condition = all[next]) }
            }
          )

          if (query.isNotEmpty() || selectedBody != null || selectedTrans != null || selectedFuel != null || selectedCond != null) {
            FilterPill(
              label = "Reset All",
              isActive = false,
              onClick = { viewModel.clearFilters() }
            )
          }
        }
      }
    }

    // Results Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Found ${vehicles.size} vehicle${if (vehicles.size == 1) "" else "s"}",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = SlateMuted
      )
    }

    // Vehicles List
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(vehicles, key = { it.id }) { vehicle ->
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
fun FilterPill(
  label: String,
  isActive: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = if (isActive) AmberGold else CarbonCard,
    border = BorderStroke(1.dp, if (isActive) AmberGold else CarbonBorder),
    modifier = Modifier.clickable { onClick() }
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
      color = if (isActive) Color(0xFF090D16) else PlatinumText,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
  }
}
