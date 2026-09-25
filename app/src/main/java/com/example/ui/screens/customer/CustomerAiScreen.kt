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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ChatMessage
import com.example.ai.MessageSender
import com.example.data.entity.VehicleEntity
import com.example.data.model.CurrencyFormatter
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
fun CustomerAiScreen(
  viewModel: MarketplaceViewModel,
  onVehicleClick: (VehicleEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val chatHistory by viewModel.customerAiMessages.collectAsStateWithLifecycle()
  val isThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

  var inputText by remember { mutableStateOf("") }

  val suggestionChips = listOf(
    "Show available luxury SUVs",
    "What V8 sports cars are in vault?",
    "Check my order status & delivery",
    "Explain escrow payment protection",
    "How does the delivery OTP handover work?"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("customer_ai_screen")
  ) {
    // Top Bar
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
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = AmberGold,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Charles Assistant",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = PlatinumText
              )
              Text(
                text = "AI Concierge • Verified Showroom Records",
                fontSize = 11.sp,
                color = AmberGold
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldSuccess.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
          ) {
            Text(
              text = "INVENTORY CONNECTED",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldSuccess,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // Chat Messages Timeline
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(chatHistory) { msg ->
        if (msg.sender == MessageSender.USER) {
          // User Message Bubble (Right aligned)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Surface(
              shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
              color = AmberGold,
              modifier = Modifier.fillMaxWidth(0.85f)
            ) {
              Text(
                text = msg.text,
                fontSize = 13.sp,
                color = Color(0xFF090D16),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(14.dp)
              )
            }
          }
        } else {
          // Assistant Message Bubble (Left aligned)
          Column(modifier = Modifier.fillMaxWidth(0.92f)) {
            Surface(
              shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
              color = CarbonCard,
              border = BorderStroke(1.dp, CarbonBorder)
            ) {
              Text(
                text = msg.text,
                fontSize = 13.sp,
                color = PlatinumText,
                lineHeight = 19.sp,
                modifier = Modifier.padding(14.dp)
              )
            }

            // If assistant returned matched vehicles, render quick cards!
            if (msg.matchedVehicles.isNotEmpty()) {
              Spacer(modifier = Modifier.height(10.dp))
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                msg.matchedVehicles.forEach { vehicle ->
                  Card(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { onVehicleClick(vehicle) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1726)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                  ) {
                    Row(
                      modifier = Modifier.padding(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = "${vehicle.year} ${vehicle.brand} ${vehicle.model}",
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                          color = PlatinumText
                        )
                        Text(
                          text = "${vehicle.horsepower} HP • ${vehicle.location}",
                          fontSize = 11.sp,
                          color = SlateMuted
                        )
                      }
                      Text(
                        text = CurrencyFormatter.formatKoboToNaira(vehicle.priceKobo),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = AmberGold
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      if (isThinking) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              color = AmberGold,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Charles Assistant querying certified vault records...", fontSize = 11.sp, color = SlateMuted)
          }
        }
      }
    }

    // Suggestion Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      suggestionChips.forEach { prompt ->
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = CarbonCard,
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.clickable {
            viewModel.sendCustomerAiMessage(prompt)
          }
        ) {
          Text(
            text = prompt,
            fontSize = 11.sp,
            color = PlatinumText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }
    }

    // Input Bar
    Surface(
      color = Color(0xF2090D16),
      border = BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 80.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = { Text("Ask Charles Assistant about vehicles, orders, checkout...", fontSize = 12.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PlatinumText,
            unfocusedTextColor = PlatinumText,
            focusedContainerColor = CarbonCard,
            unfocusedContainerColor = CarbonCard,
            focusedBorderColor = AmberGold,
            unfocusedBorderColor = CarbonBorder
          ),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("ai_input_field")
        )

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = CircleShape,
          color = if (inputText.isNotBlank()) AmberGold else Color(0xFF1B283E),
          modifier = Modifier
            .size(44.dp)
            .clickable(enabled = inputText.isNotBlank() && !isThinking) {
              val msg = inputText.trim()
              inputText = ""
              viewModel.sendCustomerAiMessage(msg)
            }
            .testTag("send_ai_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = "Send",
              tint = if (inputText.isNotBlank()) Color(0xFF090D16) else SlateMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
