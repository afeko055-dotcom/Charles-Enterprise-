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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.entity.UserEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import com.example.viewmodel.MarketplaceViewModel

@Composable
fun AccountScreen(
  viewModel: MarketplaceViewModel,
  onNavigateToOrders: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("account_screen")
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
          text = "Client Profile & Personas",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = PlatinumText
        )
        Text(
          text = "Account privileges, delivery credentials, and role-based test switching",
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
      // Current Profile Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CarbonCard),
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = Color(currentUser.role.badgeColorHex).copy(alpha = 0.2f),
              border = BorderStroke(2.dp, Color(currentUser.role.badgeColorHex)),
              modifier = Modifier.size(56.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = Color(currentUser.role.badgeColorHex),
                  modifier = Modifier.size(30.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
              Text(
                text = currentUser.fullName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = PlatinumText
              )
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(currentUser.role.badgeColorHex).copy(alpha = 0.2f),
                modifier = Modifier.padding(top = 4.dp)
              ) {
                Text(
                  text = currentUser.role.label.uppercase(),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(currentUser.role.badgeColorHex),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = currentUser.email,
                fontSize = 12.sp,
                color = SlateMuted
              )
            }
          }
        }
      }

      // Persona Switcher
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CarbonCard),
          border = BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "ROLE-BASED DEMO PERSONAS",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = AmberGold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Switch instantly to inspect customer, dispatch rider, inventory manager, finance manager, or super admin views.",
              fontSize = 11.sp,
              color = SlateMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            allUsers.forEach { user ->
              val isSelected = user.id == currentUser.id
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color(0xFF1B283E) else Color(0xFF090D16),
                border = BorderStroke(1.dp, if (isSelected) AmberGold else CarbonBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clickable { viewModel.switchUser(user) }
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(10.dp)
                        .background(Color(user.role.badgeColorHex), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PlatinumText)
                      Text(user.role.label, fontSize = 11.sp, color = Color(user.role.badgeColorHex))
                    }
                  }

                  if (isSelected) {
                    Text("ACTIVE", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AmberGold)
                  }
                }
              }
            }
          }
        }
      }

      // Security & Trust Notice
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A2E)),
          border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("DATA PRIVACY & VAULT PROTOCOL", fontWeight = FontWeight.Bold, color = EmeraldSuccess, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "• Zero banking passwords, card PINs, or token credentials collected by Apex Motors.\n• Payments verified exclusively via server-side gateway authentication.\n• Enclosed carrier deliveries protected by 6-digit cryptographic customer handover OTP.",
              fontSize = 11.sp,
              color = SlateMuted,
              lineHeight = 18.sp
            )
          }
        }
      }
    }
  }
}
