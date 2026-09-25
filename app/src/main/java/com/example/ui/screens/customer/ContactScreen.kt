package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
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
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import com.example.viewmodel.MarketplaceViewModel

@Composable
fun ContactScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  var name by remember { mutableStateOf(currentUser.fullName) }
  var email by remember { mutableStateOf(currentUser.email) }
  var phone by remember { mutableStateOf(currentUser.phone) }
  var subject by remember { mutableStateOf("Custom Import & Pre-Order Inquiry") }
  var category by remember { mutableStateOf("Sales & Custom Import") }
  var message by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("contact_screen")
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
          text = "Client Concierge & Support",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = PlatinumText
        )
        Text(
          text = "Direct hotline to our automobile brokers, vault inspectors, and dispatch managers",
          fontSize = 11.sp,
          color = SlateMuted
        )
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Concierge Hotline Cards
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ContactInfoCard(
            title = "VIP Hotline",
            value = "+234 1 800 2739",
            icon = Icons.Default.Phone,
            modifier = Modifier.weight(1f)
          )
          ContactInfoCard(
            title = "Concierge Desk",
            value = "vip@apexmotors.ng",
            icon = Icons.Default.Email,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Showroom Location
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = CarbonCard),
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Flagship Vault & Logistics Terminal", fontWeight = FontWeight.Bold, color = PlatinumText, fontSize = 13.sp)
              Text("Plot 12, Lekki Phase 1 Expressway, Victoria Island Corridor, Lagos", color = SlateMuted, fontSize = 11.sp)
            }
          }
        }
      }

      // Inquiry Form
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CarbonCard),
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Send an Inquiry to Concierge",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = PlatinumText
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = name,
              onValueChange = { name = it },
              label = { Text("Full Name") },
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PlatinumText, unfocusedTextColor = PlatinumText, focusedBorderColor = AmberGold, unfocusedBorderColor = CarbonBorder)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Email Address") },
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PlatinumText, unfocusedTextColor = PlatinumText, focusedBorderColor = AmberGold, unfocusedBorderColor = CarbonBorder)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = subject,
              onValueChange = { subject = it },
              label = { Text("Subject") },
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PlatinumText, unfocusedTextColor = PlatinumText, focusedBorderColor = AmberGold, unfocusedBorderColor = CarbonBorder)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = message,
              onValueChange = { message = it },
              label = { Text("Message / Vehicle Specifications Wanted") },
              minLines = 4,
              modifier = Modifier.fillMaxWidth().testTag("contact_message_input"),
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = PlatinumText, unfocusedTextColor = PlatinumText, focusedBorderColor = AmberGold, unfocusedBorderColor = CarbonBorder)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                if (message.isNotBlank()) {
                  viewModel.submitContact(name, email, phone, subject, category, message)
                  message = ""
                } else {
                  viewModel.showMessage("Please type your message before submitting.")
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_contact_button")
            ) {
              Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Submit Inquiry", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun ContactInfoCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = CarbonCard),
    border = BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Icon(imageVector = icon, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.height(6.dp))
      Text(title, fontSize = 11.sp, color = SlateMuted)
      Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PlatinumText)
    }
  }
}
