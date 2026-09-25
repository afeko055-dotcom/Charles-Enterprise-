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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.OrderEntity
import com.example.data.model.CurrencyFormatter
import com.example.data.model.OrderStatus
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerOrdersScreen(
  viewModel: MarketplaceViewModel,
  onTrackOrder: (OrderEntity) -> Unit,
  onBrowseVault: () -> Unit,
  modifier: Modifier = Modifier
) {
  val orders by viewModel.customerOrders.collectAsStateWithLifecycle()
  val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("customer_orders_screen")
  ) {
    // Header
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
          text = "My Vehicle Acquisitions",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = PlatinumText
        )
        Text(
          text = "Official purchase records, payment certificates, and live delivery status",
          fontSize = 11.sp,
          color = SlateMuted
        )
      }
    }

    if (orders.isEmpty()) {
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
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = SlateMuted,
              modifier = Modifier.size(36.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "No Acquisitions Yet",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = PlatinumText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Browse our certified inventory vault and acquire your luxury automobile with escrow security.",
          fontSize = 12.sp,
          color = SlateMuted,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onBrowseVault,
          colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16))
        ) {
          Text("Explore Luxury Vault", fontWeight = FontWeight.Bold)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(orders, key = { it.id }) { order ->
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("order_card_${order.id}")
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Order #${order.orderNumber}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = AmberGold
                  )
                  Text(
                    text = dateFormat.format(Date(order.createdAt)),
                    fontSize = 11.sp,
                    color = SlateMuted
                  )
                }

                StatusBadge(
                  text = order.status.label,
                  color = when (order.status) {
                    OrderStatus.CONFIRMED, OrderStatus.PAID -> EmeraldSuccess
                    OrderStatus.DELIVERED -> ElectricCyan
                    OrderStatus.CANCELLED, OrderStatus.REFUNDED -> CrimsonAlert
                    else -> AmberGold
                  }
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = order.vehicleTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PlatinumText
              )
              Text(
                text = "VIN: ${order.vehicleVin}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = SlateMuted
              )

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("Total Amount Paid", fontSize = 10.sp, color = SlateMuted)
                  Text(
                    text = CurrencyFormatter.formatKoboToNaira(order.totalAmountKobo),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = AmberGold
                  )
                }

                // Track Carrier Action Button
                if (order.status == OrderStatus.CONFIRMED || order.status == OrderStatus.PROCESSING || order.status == OrderStatus.DISPATCHED) {
                  Button(
                    onClick = { onTrackOrder(order) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF00253B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("track_button_${order.id}")
                  ) {
                    Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track Flatbed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                } else if (order.status == OrderStatus.DELIVERED) {
                  OutlinedButton(
                    onClick = { onTrackOrder(order) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldSuccess),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Text("View Certificate", fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
