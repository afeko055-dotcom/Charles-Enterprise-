package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.OrderEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.VehicleEntity
import com.example.data.model.UserRole
import com.example.ui.components.CharlesTopBar
import com.example.ui.screens.LaunchScreen
import com.example.ui.screens.admin.AdminAuthScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.customer.AccountScreen
import com.example.ui.screens.customer.CheckoutHostedScreen
import com.example.ui.screens.customer.ContactScreen
import com.example.ui.screens.customer.CustomerOrdersScreen
import com.example.ui.screens.customer.HomeScreen
import com.example.ui.screens.customer.LiveTrackingScreen
import com.example.ui.screens.customer.SavedCarsScreen
import com.example.ui.screens.customer.SearchFilterScreen
import com.example.ui.screens.customer.VehicleDetailScreen
import com.example.ui.screens.rider.RiderDashboardScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import com.example.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppDestination(val label: String) {
  HOME("Showroom"),
  SEARCH("Catalog"),
  SAVED("Saved"),
  ORDERS("Orders"),
  ACCOUNT("Profile"),
  CONTACT("Concierge"),
  VEHICLE_DETAIL("Detail"),
  CHECKOUT_HOSTED("Hosted Checkout"),
  TRACKING("Live Tracking"),
  RIDER_TERMINAL("Rider Hub"),
  ADMIN_AUTH("Admin Gateway"),
  ADMIN_CONSOLE("Admin Console")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
  viewModel: MarketplaceViewModel,
  initialRoute: String? = null,
  modifier: Modifier = Modifier
) {
  var showLaunchScreen by remember { mutableStateOf(initialRoute == null) }

  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()
  val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

  val startDestination = if (initialRoute == "/admin") AppDestination.ADMIN_AUTH else AppDestination.HOME
  var currentDestination by remember { mutableStateOf(startDestination) }
  var screenStack by remember { mutableStateOf(listOf(startDestination)) }

  LaunchedEffect(initialRoute) {
    if (initialRoute == "/admin") {
      showLaunchScreen = false
      currentDestination = AppDestination.ADMIN_AUTH
      screenStack = listOf(AppDestination.ADMIN_AUTH)
    }
  }

  val selectedVehicle by viewModel.selectedVehicle.collectAsStateWithLifecycle()
  val trackingOrder by viewModel.trackingOrder.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  var showNotificationsModal by remember { mutableStateOf(false) }

  // Listen to snackbar messages from ViewModel
  LaunchedEffect(Unit) {
    viewModel.snackbarMessage.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  fun navigateTo(dest: AppDestination) {
    screenStack = screenStack + dest
    currentDestination = dest
  }

  fun navigateBack() {
    if (screenStack.size > 1) {
      screenStack = screenStack.dropLast(1)
      currentDestination = screenStack.last()
    } else {
      currentDestination = AppDestination.HOME
    }
  }

  // Handle system back button for all sub-screens
  BackHandler(enabled = currentDestination != AppDestination.HOME) {
    navigateBack()
  }

  // Auto route if user persona is switched
  LaunchedEffect(currentUser.role) {
    when (currentUser.role) {
      UserRole.RIDER -> {
        if (currentDestination != AppDestination.RIDER_TERMINAL) {
          navigateTo(AppDestination.RIDER_TERMINAL)
        }
      }
      UserRole.SUPER_ADMIN, UserRole.FINANCE_MANAGER, UserRole.INVENTORY_MANAGER, UserRole.ORDER_MANAGER, UserRole.DISPATCH_MANAGER -> {
        if (currentDestination != AppDestination.ADMIN_CONSOLE && currentDestination != AppDestination.ADMIN_AUTH) {
          navigateTo(AppDestination.ADMIN_CONSOLE)
        }
      }
      else -> {
        if (currentDestination == AppDestination.RIDER_TERMINAL || currentDestination == AppDestination.ADMIN_CONSOLE) {
          currentDestination = AppDestination.HOME
          screenStack = listOf(AppDestination.HOME)
        }
      }
    }
  }

  if (showLaunchScreen) {
    LaunchScreen(
      onLaunchComplete = { showLaunchScreen = false }
    )
    return
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = CarbonDark,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      CharlesTopBar(
        currentUser = currentUser,
        allUsers = allUsers,
        unreadNotificationsCount = unreadCount,
        onUserSwitch = { newUser: UserEntity -> viewModel.switchUser(newUser) },
        onNotificationsClick = { showNotificationsModal = true }
      )
    },
    bottomBar = {
      // Bottom Navigation Bar for Customer
      val isCustomer = currentUser.role == UserRole.CUSTOMER || currentUser.role == UserRole.SUPPORT
      if (isCustomer && (currentDestination == AppDestination.HOME || currentDestination == AppDestination.SEARCH || currentDestination == AppDestination.SAVED || currentDestination == AppDestination.ORDERS || currentDestination == AppDestination.ACCOUNT || currentDestination == AppDestination.CONTACT)) {
        NavigationBar(
          containerColor = Color(0xF2090D16),
          contentColor = PlatinumText,
          modifier = Modifier
            .navigationBarsPadding()
            .testTag("customer_bottom_nav")
        ) {
          NavigationBarItem(
            selected = currentDestination == AppDestination.HOME,
            onClick = {
              currentDestination = AppDestination.HOME
              screenStack = listOf(AppDestination.HOME)
            },
            icon = { Icon(if (currentDestination == AppDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Showroom") },
            label = { Text("Vault", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = AmberGold,
              selectedTextColor = AmberGold,
              indicatorColor = Color(0xFF1B283E),
              unselectedIconColor = SlateMuted,
              unselectedTextColor = SlateMuted
            )
          )

          NavigationBarItem(
            selected = currentDestination == AppDestination.SEARCH,
            onClick = {
              currentDestination = AppDestination.SEARCH
              screenStack = listOf(AppDestination.SEARCH)
            },
            icon = { Icon(Icons.Filled.Search, contentDescription = "Catalog") },
            label = { Text("Catalog", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = AmberGold,
              selectedTextColor = AmberGold,
              indicatorColor = Color(0xFF1B283E),
              unselectedIconColor = SlateMuted,
              unselectedTextColor = SlateMuted
            )
          )

          NavigationBarItem(
            selected = currentDestination == AppDestination.SAVED,
            onClick = {
              currentDestination = AppDestination.SAVED
              screenStack = listOf(AppDestination.SAVED)
            },
            icon = { Icon(if (currentDestination == AppDestination.SAVED) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Saved") },
            label = { Text("Wishlist", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = AmberGold,
              selectedTextColor = AmberGold,
              indicatorColor = Color(0xFF1B283E),
              unselectedIconColor = SlateMuted,
              unselectedTextColor = SlateMuted
            )
          )

          NavigationBarItem(
            selected = currentDestination == AppDestination.ORDERS,
            onClick = {
              currentDestination = AppDestination.ORDERS
              screenStack = listOf(AppDestination.ORDERS)
            },
            icon = { Icon(if (currentDestination == AppDestination.ORDERS) Icons.Filled.Receipt else Icons.Outlined.Receipt, contentDescription = "Orders") },
            label = { Text("Orders", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = AmberGold,
              selectedTextColor = AmberGold,
              indicatorColor = Color(0xFF1B283E),
              unselectedIconColor = SlateMuted,
              unselectedTextColor = SlateMuted
            )
          )

          NavigationBarItem(
            selected = currentDestination == AppDestination.CONTACT,
            onClick = {
              currentDestination = AppDestination.CONTACT
              screenStack = listOf(AppDestination.CONTACT)
            },
            icon = { Icon(Icons.Filled.SupportAgent, contentDescription = "Concierge") },
            label = { Text("Concierge", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = AmberGold,
              selectedTextColor = AmberGold,
              indicatorColor = Color(0xFF1B283E),
              unselectedIconColor = SlateMuted,
              unselectedTextColor = SlateMuted
            )
          )

          NavigationBarItem(
            selected = currentDestination == AppDestination.ACCOUNT,
            onClick = {
              currentDestination = AppDestination.ACCOUNT
              screenStack = listOf(AppDestination.ACCOUNT)
            },
            icon = { Icon(if (currentDestination == AppDestination.ACCOUNT) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Account") },
            label = { Text("Profile", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = AmberGold,
              selectedTextColor = AmberGold,
              indicatorColor = Color(0xFF1B283E),
              unselectedIconColor = SlateMuted,
              unselectedTextColor = SlateMuted
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentDestination) {
        AppDestination.HOME -> {
          HomeScreen(
            viewModel = viewModel,
            onVehicleClick = { vehicle ->
              viewModel.selectVehicle(vehicle)
              navigateTo(AppDestination.VEHICLE_DETAIL)
            },
            onNavigateToSearch = { navigateTo(AppDestination.SEARCH) }
          )
        }

        AppDestination.SEARCH -> {
          SearchFilterScreen(
            viewModel = viewModel,
            onVehicleClick = { vehicle ->
              viewModel.selectVehicle(vehicle)
              navigateTo(AppDestination.VEHICLE_DETAIL)
            },
            onNavigateToAdmin = {
              navigateTo(AppDestination.ADMIN_AUTH)
            }
          )
        }

        AppDestination.SAVED -> {
          SavedCarsScreen(
            viewModel = viewModel,
            onVehicleClick = { vehicle ->
              viewModel.selectVehicle(vehicle)
              navigateTo(AppDestination.VEHICLE_DETAIL)
            },
            onExploreClick = { navigateTo(AppDestination.HOME) }
          )
        }

        AppDestination.ORDERS -> {
          CustomerOrdersScreen(
            viewModel = viewModel,
            onTrackOrder = { order ->
              viewModel.trackingOrder.value = order
              navigateTo(AppDestination.TRACKING)
            },
            onBrowseVault = { navigateTo(AppDestination.HOME) }
          )
        }

        AppDestination.CONTACT -> {
          ContactScreen(viewModel = viewModel)
        }

        AppDestination.ACCOUNT -> {
          AccountScreen(
            viewModel = viewModel,
            onNavigateToOrders = { navigateTo(AppDestination.ORDERS) }
          )
        }

        AppDestination.VEHICLE_DETAIL -> {
          if (selectedVehicle != null) {
            VehicleDetailScreen(
              vehicle = selectedVehicle!!,
              viewModel = viewModel,
              onBack = { navigateBack() },
              onProceedToHostedCheckout = { navigateTo(AppDestination.CHECKOUT_HOSTED) }
            )
          } else {
            currentDestination = AppDestination.HOME
          }
        }

        AppDestination.CHECKOUT_HOSTED -> {
          CheckoutHostedScreen(
            viewModel = viewModel,
            onNavigateToTracking = {
              currentDestination = AppDestination.TRACKING
              screenStack = listOf(AppDestination.HOME, AppDestination.TRACKING)
            },
            onCancel = {
              navigateBack()
            }
          )
        }

        AppDestination.TRACKING -> {
          if (trackingOrder != null) {
            LiveTrackingScreen(
              order = trackingOrder!!,
              viewModel = viewModel,
              onBack = { navigateBack() }
            )
          } else {
            currentDestination = AppDestination.ORDERS
          }
        }

        AppDestination.RIDER_TERMINAL -> {
          RiderDashboardScreen(viewModel = viewModel)
        }

        AppDestination.ADMIN_AUTH -> {
          AdminAuthScreen(
            viewModel = viewModel,
            onAuthenticated = { adminUser ->
              currentDestination = AppDestination.ADMIN_CONSOLE
              screenStack = listOf(AppDestination.ADMIN_CONSOLE)
            },
            onExit = {
              currentDestination = AppDestination.HOME
              screenStack = listOf(AppDestination.HOME)
            }
          )
        }

        AppDestination.ADMIN_CONSOLE -> {
          AdminDashboardScreen(viewModel = viewModel)
        }
      }
    }

    // Notifications Bottom Sheet Modal
    if (showNotificationsModal) {
      ModalBottomSheet(
        onDismissRequest = {
          showNotificationsModal = false
          viewModel.markNotificationsRead()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CarbonCard
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "In-App Notifications",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = PlatinumText
            )

            Text(
              text = "Mark all read",
              fontSize = 12.sp,
              color = AmberGold,
              modifier = Modifier.clickable { viewModel.markNotificationsRead() }
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (notifications.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("No new notifications", color = SlateMuted, fontSize = 13.sp)
            }
          } else {
            val dateFmt = SimpleDateFormat("HH:mm, dd MMM", Locale.US)
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(notifications, key = { it.id }) { notif ->
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (notif.isRead) Color(0xFF090D16) else Color(0xFF1B283E),
                  border = BorderStroke(1.dp, CarbonBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PlatinumText)
                      Text(dateFmt.format(Date(notif.createdAt)), fontSize = 10.sp, color = SlateMuted)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(notif.message, fontSize = 12.sp, color = SlateMuted)
                  }
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
        }
      }
    }
  }
}
