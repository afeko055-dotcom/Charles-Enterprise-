package com.example.ui.screens.admin

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.entity.ContactMessageEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.VehicleEntity
import com.example.data.model.BodyType
import com.example.data.model.CurrencyFormatter
import com.example.data.model.FuelType
import com.example.data.model.LedgerDirection
import com.example.data.model.OrderStatus
import com.example.data.model.TransmissionType
import com.example.data.model.VehicleCondition
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun AdminDashboardScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val vehicles by viewModel.allVehicles.collectAsStateWithLifecycle()
  val orders by viewModel.allOrders.collectAsStateWithLifecycle()
  val deliveries by viewModel.allDeliveries.collectAsStateWithLifecycle()
  val financialSummary by viewModel.financialSummary.collectAsStateWithLifecycle()
  val ledgerEntries by viewModel.allLedgerEntries.collectAsStateWithLifecycle()
  val withdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()
  val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
  val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
  val contactMessages by viewModel.contactMessages.collectAsStateWithLifecycle()
  val reconciliation by viewModel.reconciliationResult.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Overview", "Inventory", "Orders", "Finance & Payouts", "Audit Trail", "Inquiries")

  // Modals state
  var showAddVehicleDialog by remember { mutableStateOf(false) }
  var vehicleToEdit by remember { mutableStateOf<VehicleEntity?>(null) }
  var showWithdrawalDialog by remember { mutableStateOf(false) }
  var showRefundDialog by remember { mutableStateOf<OrderEntity?>(null) }
  var refundReason by remember { mutableStateOf("Client requested vehicle specification upgrade") }
  var replyMessageTarget by remember { mutableStateOf<ContactMessageEntity?>(null) }
  var replyText by remember { mutableStateOf("") }

  val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.US)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .testTag("admin_dashboard_screen")
  ) {
    // Admin Top Banner
    Surface(
      color = Color(0xF2090D16),
      border = BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Management & Finance Console",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = PlatinumText
            )
            Text(
              text = "${currentUser.fullName} (${currentUser.role.label})",
              fontSize = 11.sp,
              color = AmberGold
            )
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = AmberGold.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, AmberGold)
          ) {
            Text(
              text = "SUPERVISOR MODE",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = AmberGold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          contentColor = AmberGold,
          edgePadding = 0.dp,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = AmberGold
            )
          }
        ) {
          tabs.forEachIndexed { idx, title ->
            Tab(
              selected = selectedTab == idx,
              onClick = { selectedTab = idx },
              text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium) }
            )
          }
        }
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      when (selectedTab) {
        0 -> {
          // --- OVERVIEW TAB ---
          // 1. KPI Cards
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              AdminKpiCard(
                title = "Total Gross Revenue",
                value = CurrencyFormatter.formatKoboToNaira(financialSummary.totalSalesKobo),
                color = AmberGold,
                modifier = Modifier.weight(1f)
              )
              AdminKpiCard(
                title = "Available for Payout",
                value = CurrencyFormatter.formatKoboToNaira(financialSummary.availableFundsKobo),
                color = EmeraldSuccess,
                modifier = Modifier.weight(1f)
              )
            }
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              AdminKpiCard(
                title = "Pending Clearing",
                value = CurrencyFormatter.formatKoboToNaira(financialSummary.pendingFundsKobo),
                color = ElectricCyan,
                modifier = Modifier.weight(1f)
              )
              AdminKpiCard(
                title = "Total Withdrawn",
                value = CurrencyFormatter.formatKoboToNaira(financialSummary.totalWithdrawnKobo),
                color = SlateMuted,
                modifier = Modifier.weight(1f)
              )
            }
          }

          // 2. Settlement & Reconciliation Control Card
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = CarbonCard),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "TREASURY & AUDIT RECONCILIATION",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = AmberGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Strict financial governance: Compare application orders, payment gateway records, and ledger credits to verify zero discrepancies.",
                  fontSize = 11.sp,
                  color = SlateMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Button(
                    onClick = { viewModel.settlePendingFunds() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F), contentColor = ElectricCyan),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("settle_funds_button")
                  ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run Settlement", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }

                  Button(
                    onClick = { viewModel.runReconciliation() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("reconcile_button")
                  ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reconcile Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }

                if (reconciliation != null) {
                  val rec = reconciliation!!
                  Spacer(modifier = Modifier.height(12.dp))
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (rec.isReconciled) EmeraldSuccess.copy(alpha = 0.15f) else CrimsonAlert.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (rec.isReconciled) EmeraldSuccess else CrimsonAlert),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Text(
                        text = if (rec.isReconciled) "AUDIT VERIFIED: 100% RECONCILED (0 NGN Discrepancy)" else "DISCREPANCY FLAGGED: ₦${rec.discrepancyKobo / 100}",
                        fontWeight = FontWeight.Bold,
                        color = if (rec.isReconciled) EmeraldSuccess else CrimsonAlert,
                        fontSize = 12.sp
                      )
                      Text(
                        text = "Orders Paid: ${CurrencyFormatter.formatKoboToNaira(rec.totalPaidOrdersAmountKobo)} • Gateway: ${CurrencyFormatter.formatKoboToNaira(rec.totalPaymentGatewayAmountKobo)} • Ledger: ${CurrencyFormatter.formatKoboToNaira(rec.totalLedgerSalesKobo)}",
                        fontSize = 10.sp,
                        color = PlatinumText
                      )
                    }
                  }
                }
              }
            }
          }

          // 3. Quick Stats
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = CarbonCard),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text("OPERATIONAL HEALTH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateMuted)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Total Vehicles in Vault", fontSize = 12.sp, color = SlateMuted)
                  Text("${vehicles.size} units", fontWeight = FontWeight.Bold, color = PlatinumText, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Available for Sale", fontSize = 12.sp, color = SlateMuted)
                  Text("${vehicles.count { it.status == VehicleStatus.AVAILABLE }} units", fontWeight = FontWeight.Bold, color = EmeraldSuccess, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Sold Units", fontSize = 12.sp, color = SlateMuted)
                  Text("${vehicles.count { it.status == VehicleStatus.SOLD || it.status == VehicleStatus.DELIVERED }} units", fontWeight = FontWeight.Bold, color = AmberGold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Active Carrier Dispatches", fontSize = 12.sp, color = SlateMuted)
                  Text("${deliveries.count { it.status != com.example.data.model.DeliveryStatus.DELIVERED }} active", fontWeight = FontWeight.Bold, color = ElectricCyan, fontSize = 12.sp)
                }
              }
            }
          }
        }

        1 -> {
          // --- INVENTORY TAB ---
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Vehicle Inventory Vault (${vehicles.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PlatinumText
              )

              Button(
                onClick = {
                  vehicleToEdit = null
                  showAddVehicleDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_vehicle_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Vehicle", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }

          items(vehicles, key = { it.id }) { vehicle ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = CarbonCard),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "${vehicle.year} ${vehicle.brand} ${vehicle.model}",
                      fontWeight = FontWeight.Bold,
                      color = PlatinumText,
                      fontSize = 14.sp
                    )
                    Text("VIN: ${vehicle.vin}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SlateMuted)
                  }

                  StatusBadge(
                    text = vehicle.status.label,
                    color = when (vehicle.status) {
                      VehicleStatus.AVAILABLE -> EmeraldSuccess
                      VehicleStatus.RESERVED -> AmberGold
                      VehicleStatus.SOLD -> CrimsonAlert
                      else -> ElectricCyan
                    }
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = CurrencyFormatter.formatKoboToNaira(vehicle.priceKobo),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = AmberGold
                  )

                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                      onClick = {
                        vehicleToEdit = vehicle
                        showAddVehicleDialog = true
                      },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SlateMuted, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                      onClick = { viewModel.deleteVehicle(vehicle) },
                      modifier = Modifier.size(32.dp)
                    ) {
                      Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                    }
                  }
                }
              }
            }
          }
        }

        2 -> {
          // --- ORDERS TAB ---
          item {
            Text(
              text = "Customer Purchase Orders (${orders.size})",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = PlatinumText
            )
          }

          items(orders, key = { it.id }) { order ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = CarbonCard),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold, color = AmberGold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                  StatusBadge(text = order.status.label, color = if (order.status == OrderStatus.CONFIRMED || order.status == OrderStatus.PAID) EmeraldSuccess else AmberGold)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(order.vehicleTitle, fontWeight = FontWeight.Bold, color = PlatinumText, fontSize = 14.sp)
                Text("Address: ${order.deliveryAddress}, ${order.deliveryCity}", fontSize = 11.sp, color = SlateMuted)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(CurrencyFormatter.formatKoboToNaira(order.totalAmountKobo), fontWeight = FontWeight.Black, color = AmberGold, fontSize = 15.sp)

                  // Refund Trigger
                  if (order.status == OrderStatus.CONFIRMED || order.status == OrderStatus.PAID) {
                    OutlinedButton(
                      onClick = { showRefundDialog = order },
                      colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAlert),
                      border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.5f)),
                      shape = RoundedCornerShape(8.dp)
                    ) {
                      Text("Issue Refund", fontSize = 11.sp)
                    }
                  }
                }
              }
            }
          }
        }

        3 -> {
          // --- FINANCE & PAYOUTS TAB ---
          item {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF1E283E)),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text("Available Withdrawable Balance", fontSize = 11.sp, color = SlateMuted)
                    Text(
                      text = CurrencyFormatter.formatKoboToNaira(financialSummary.availableFundsKobo),
                      fontSize = 24.sp,
                      fontWeight = FontWeight.Black,
                      color = EmeraldSuccess
                    )
                  }

                  Button(
                    onClick = { showWithdrawalDialog = true },
                    enabled = financialSummary.availableFundsKobo > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("request_payout_button")
                  ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Payout", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }
              }
            }
          }

          item {
            Text("Ledger Transactions (Double-Entry Audit)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PlatinumText)
          }

          items(ledgerEntries, key = { it.id }) { entry ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = CarbonCard),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(entry.description, fontWeight = FontWeight.Bold, color = PlatinumText, fontSize = 12.sp)
                  Text("Ref: ${entry.reference} • ${dateFormat.format(Date(entry.createdAt))}", fontSize = 10.sp, color = SlateMuted)
                }

                val isCredit = entry.direction == LedgerDirection.CREDIT
                Text(
                  text = "${if (isCredit) "+" else "-"}${CurrencyFormatter.formatKoboToNaira(entry.netAmountKobo)}",
                  fontWeight = FontWeight.Black,
                  fontSize = 13.sp,
                  color = if (isCredit) EmeraldSuccess else CrimsonAlert
                )
              }
            }
          }
        }

        4 -> {
          // --- AUDIT TRAIL TAB ---
          item {
            Text("Immutable Security Audit Logs (${auditLogs.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PlatinumText)
          }

          items(auditLogs, key = { it.id }) { log ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = CarbonCard),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AmberGold)
                  Text(dateFormat.format(Date(log.createdAt)), fontSize = 10.sp, color = SlateMuted)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(log.details, fontSize = 12.sp, color = PlatinumText)
                Text("Actor: ${log.actorName} (${log.actorRole}) • IP: ${log.ipAddress}", fontSize = 10.sp, color = SlateMuted)
              }
            }
          }
        }

        5 -> {
          // --- INQUIRIES TAB ---
          item {
            Text("Client Concierge Inquiries (${contactMessages.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PlatinumText)
          }

          items(contactMessages, key = { it.id }) { msg ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = CarbonCard),
              border = BorderStroke(1.dp, CarbonBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(msg.subject, fontWeight = FontWeight.Bold, color = AmberGold, fontSize = 13.sp)
                  StatusBadge(text = msg.status, color = if (msg.status == "RESOLVED") EmeraldSuccess else ElectricCyan)
                }
                Text("From: ${msg.name} (${msg.email}) • Phone: ${msg.phone}", fontSize = 11.sp, color = SlateMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Text(msg.message, fontSize = 12.sp, color = PlatinumText)

                if (msg.adminReply != null) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF090D16), modifier = Modifier.fillMaxWidth()) {
                    Text("Staff Reply: ${msg.adminReply}", fontSize = 11.sp, color = EmeraldSuccess, modifier = Modifier.padding(8.dp))
                  }
                } else {
                  Spacer(modifier = Modifier.height(8.dp))
                  Button(
                    onClick = {
                      replyMessageTarget = msg
                      replyText = "Thank you for reaching out to Apex Motors Concierge. A dedicated private broker has been assigned to your vehicle request."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16)),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("Respond", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }
    }

    // Modal Dialog: Add/Edit Vehicle
    if (showAddVehicleDialog) {
      var brand by remember { mutableStateOf(vehicleToEdit?.brand ?: "Mercedes-AMG") }
      var model by remember { mutableStateOf(vehicleToEdit?.model ?: "GT 63 S E-Performance") }
      var yearStr by remember { mutableStateOf(vehicleToEdit?.year?.toString() ?: "2024") }
      var priceMillions by remember { mutableStateOf(vehicleToEdit?.let { (it.priceKobo / 100_000_000L).toString() } ?: "245") }
      var mileageStr by remember { mutableStateOf(vehicleToEdit?.mileageKm?.toString() ?: "1500") }
      var description by remember { mutableStateOf(vehicleToEdit?.description ?: "4.0L V8 Biturbo with electric motor creating 831 combined hp.") }
      var location by remember { mutableStateOf(vehicleToEdit?.location ?: "Lekki Flagship Hub, Lagos") }
      var vin by remember { mutableStateOf(vehicleToEdit?.vin ?: "WDB982140A" + UUID.randomUUID().toString().take(6).uppercase()) }
      var isFeatured by remember { mutableStateOf(vehicleToEdit?.featured ?: true) }

      AlertDialog(
        onDismissRequest = { showAddVehicleDialog = false },
        containerColor = CarbonCard,
        title = { Text(if (vehicleToEdit == null) "Add Luxury Vehicle" else "Edit Vehicle", color = PlatinumText) },
        text = {
          LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
              OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth())
            }
            item {
              OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
            }
            item {
              OutlinedTextField(value = yearStr, onValueChange = { yearStr = it }, label = { Text("Year") }, modifier = Modifier.fillMaxWidth())
            }
            item {
              OutlinedTextField(value = priceMillions, onValueChange = { priceMillions = it }, label = { Text("Price (in Millions ₦)") }, modifier = Modifier.fillMaxWidth())
            }
            item {
              OutlinedTextField(value = mileageStr, onValueChange = { mileageStr = it }, label = { Text("Mileage (km)") }, modifier = Modifier.fillMaxWidth())
            }
            item {
              OutlinedTextField(value = vin, onValueChange = { vin = it }, label = { Text("VIN") }, modifier = Modifier.fillMaxWidth())
            }
            item {
              OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val pKobo = (priceMillions.toLongOrNull() ?: 200L) * 1_000_000_00L
              val v = VehicleEntity(
                id = vehicleToEdit?.id ?: ("veh_" + UUID.randomUUID().toString().take(8)),
                brand = brand,
                model = model,
                year = yearStr.toIntOrNull() ?: 2024,
                priceKobo = pKobo,
                mileageKm = mileageStr.toIntOrNull() ?: 1000,
                transmission = TransmissionType.AUTOMATIC,
                fuelType = FuelType.PETROL,
                bodyType = BodyType.COUPE,
                color = "Obsidian Black",
                condition = VehicleCondition.BRAND_NEW,
                description = description,
                location = location,
                vin = vin,
                engineSpecs = "4.0L Handcrafted Biturbo",
                horsepower = 650,
                acceleration0to100 = 3.2f,
                imageResName = "img_coupe_supercar",
                featured = isFeatured
              )
              viewModel.saveVehicle(v, isNew = vehicleToEdit == null)
              showAddVehicleDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16))
          ) {
            Text("Save Vehicle")
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showAddVehicleDialog = false }) { Text("Cancel") }
        }
      )
    }

    // Modal Dialog: Request Bank Withdrawal
    if (showWithdrawalDialog) {
      val defaultAccount = bankAccounts.firstOrNull() ?: com.example.data.entity.BankAccountEntity("bank_1", "Guaranty Trust Bank", "058", "******4819", "APEX MOTORS NIGERIA LTD", "RCP_gtb_9812491", true)
      var selectedBankId by remember { mutableStateOf(defaultAccount.id) }
      val maxWithdrawMillions = (financialSummary.availableFundsKobo / 100_000_000L).coerceAtLeast(1)
      var withdrawMillionsStr by remember { mutableStateOf(maxWithdrawMillions.toString()) }

      AlertDialog(
        onDismissRequest = { showWithdrawalDialog = false },
        containerColor = CarbonCard,
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AmberGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Request Bank Payout", color = PlatinumText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }
        },
        text = {
          Column {
            Text("Available for Withdrawal: ${CurrencyFormatter.formatKoboToNaira(financialSummary.availableFundsKobo)}", fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Select Verified Business Bank Account:", fontSize = 11.sp, color = SlateMuted)
            bankAccounts.forEach { acc ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedBankId = acc.id }
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = selectedBankId == acc.id,
                  onClick = { selectedBankId = acc.id },
                  colors = RadioButtonDefaults.colors(selectedColor = AmberGold)
                )
                Column {
                  Text(acc.bankName, fontSize = 12.sp, color = PlatinumText, fontWeight = FontWeight.Bold)
                  Text("Account: ${acc.accountNumberMasked} (${acc.accountHolderName})", fontSize = 10.sp, color = SlateMuted)
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = withdrawMillionsStr,
              onValueChange = { withdrawMillionsStr = it },
              label = { Text("Payout Amount (in Millions ₦)") },
              modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input")
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val amtKobo = (withdrawMillionsStr.toLongOrNull() ?: 10L) * 1_000_000_00L
              viewModel.requestWithdrawal(selectedBankId, amtKobo)
              showWithdrawalDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
            modifier = Modifier.testTag("submit_withdrawal_button")
          ) {
            Text("Authorize Bank Payout")
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showWithdrawalDialog = false }) { Text("Cancel") }
        }
      )
    }

    // Modal Dialog: Order Refund
    if (showRefundDialog != null) {
      val order = showRefundDialog!!
      AlertDialog(
        onDismissRequest = { showRefundDialog = null },
        containerColor = CarbonCard,
        title = { Text("Process Order Refund", color = CrimsonAlert) },
        text = {
          Column {
            Text("Order #${order.orderNumber} • ${order.vehicleTitle}", fontSize = 13.sp, color = PlatinumText, fontWeight = FontWeight.Bold)
            Text("Amount: ${CurrencyFormatter.formatKoboToNaira(order.totalAmountKobo)}", fontSize = 12.sp, color = AmberGold)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = refundReason,
              onValueChange = { refundReason = it },
              label = { Text("Reason for Refund") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.processRefund(order.id, refundReason)
              showRefundDialog = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert, contentColor = Color.White)
          ) {
            Text("Confirm Refund & Restock")
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showRefundDialog = null }) { Text("Cancel") }
        }
      )
    }

    // Reply inquiry modal
    if (replyMessageTarget != null) {
      val msg = replyMessageTarget!!
      AlertDialog(
        onDismissRequest = { replyMessageTarget = null },
        containerColor = CarbonCard,
        title = { Text("Reply to Client ${msg.name}", color = PlatinumText) },
        text = {
          Column {
            Text("Inquiry: ${msg.message}", fontSize = 12.sp, color = SlateMuted)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = replyText,
              onValueChange = { replyText = it },
              label = { Text("Staff Concierge Response") },
              minLines = 3,
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              viewModel.replyContact(msg, replyText)
              replyMessageTarget = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color(0xFF090D16))
          ) {
            Text("Send Response")
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { replyMessageTarget = null }) { Text("Cancel") }
        }
      )
    }
  }
}

@Composable
fun AdminKpiCard(
  title: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = CarbonCard),
    border = BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(title, fontSize = 11.sp, color = SlateMuted, maxLines = 1)
      Spacer(modifier = Modifier.height(6.dp))
      Text(value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = color, maxLines = 1)
    }
  }
}
