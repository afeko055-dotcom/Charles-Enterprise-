package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserEntity
import com.example.data.entity.VehicleEntity
import com.example.data.model.CurrencyFormatter
import com.example.data.model.DeliveryStatus
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleStatus
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharlesTopBar(
  currentUser: UserEntity,
  allUsers: List<UserEntity>,
  unreadNotificationsCount: Int,
  onUserSwitch: (UserEntity) -> Unit,
  onNotificationsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var userMenuExpanded by remember { mutableStateOf(false) }

  TopAppBar(
    modifier = modifier.testTag("charles_top_bar"),
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = CarbonDark,
      titleContentColor = PlatinumText
    ),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = AmberGold.copy(alpha = 0.2f),
          border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = "Charles Enterprise Emblem",
              tint = AmberGold,
              modifier = Modifier.size(22.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "CHARLES",
              fontWeight = FontWeight.Black,
              letterSpacing = 2.sp,
              fontSize = 16.sp,
              color = PlatinumText
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "ENTERPRISE",
              fontWeight = FontWeight.Light,
              letterSpacing = 2.sp,
              fontSize = 16.sp,
              color = AmberGold
            )
          }
          Text(
            text = "AUTOMOBILE VAULT & CONCIERGE",
            fontSize = 8.5.sp,
            letterSpacing = 1.sp,
            color = SlateMuted,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    },
    actions = {
      // Role Switcher Pill
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(currentUser.role.badgeColorHex).copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Color(currentUser.role.badgeColorHex).copy(alpha = 0.4f)),
        modifier = Modifier
          .padding(end = 4.dp)
          .clickable { userMenuExpanded = true }
          .testTag("role_switcher_pill")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(Color(currentUser.role.badgeColorHex))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = currentUser.role.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumText
          )
        }
      }

      DropdownMenu(
        expanded = userMenuExpanded,
        onDismissRequest = { userMenuExpanded = false },
        modifier = Modifier.background(CarbonCard)
      ) {
        Text(
          text = "Switch User Persona",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = SlateMuted,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        allUsers.forEach { user ->
          DropdownMenuItem(
            text = {
              Column {
                Text(
                  text = user.fullName,
                  fontWeight = FontWeight.SemiBold,
                  color = PlatinumText,
                  fontSize = 14.sp
                )
                Text(
                  text = user.role.label,
                  fontSize = 12.sp,
                  color = Color(user.role.badgeColorHex)
                )
              }
            },
            onClick = {
              userMenuExpanded = false
              onUserSwitch(user)
            }
          )
        }
      }

      // Notifications Bell
      IconButton(
        onClick = onNotificationsClick,
        modifier = Modifier.testTag("notifications_button")
      ) {
        BadgedBox(
          badge = {
            if (unreadNotificationsCount > 0) {
              Badge(containerColor = CrimsonAlert) {
                Text(unreadNotificationsCount.toString(), color = Color.White)
              }
            }
          }
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = PlatinumText
          )
        }
      }
    }
  )
}

@Composable
fun VehicleCard(
  vehicle: VehicleEntity,
  isSaved: Boolean,
  onCardClick: () -> Unit,
  onSaveClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val imageResId = remember(vehicle.imageResName) {
    val id = context.resources.getIdentifier(vehicle.imageResName, "drawable", context.packageName)
    if (id != 0) id else context.resources.getIdentifier("img_hero_car", "drawable", context.packageName)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("vehicle_card_${vehicle.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CarbonCard),
    border = BorderStroke(1.dp, CarbonBorder)
  ) {
    Column {
      // Vehicle Image Container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp)
      ) {
        if (imageResId != 0) {
          Image(
            painter = painterResource(id = imageResId),
            contentDescription = "${vehicle.brand} ${vehicle.model}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF161F30)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = null,
              tint = AmberGold,
              modifier = Modifier.size(54.dp)
            )
          }
        }

        // Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x99000000), Color(0xEE090D16)),
                startY = 100f
              )
            )
        )

        // Status & Condition Badges Top-Left
        Row(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(10.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (vehicle.status) {
              VehicleStatus.AVAILABLE -> EmeraldSuccess.copy(alpha = 0.9f)
              VehicleStatus.RESERVED, VehicleStatus.PAYMENT_PENDING -> AmberGold.copy(alpha = 0.9f)
              VehicleStatus.SOLD, VehicleStatus.DELIVERED -> CrimsonAlert.copy(alpha = 0.9f)
              VehicleStatus.IN_DELIVERY -> ElectricCyan.copy(alpha = 0.9f)
            }
          ) {
            Text(
              text = vehicle.status.label.uppercase(),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          if (vehicle.featured) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFD97706)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "FEATURED",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
        }

        // Bookmark / Favorite Button Top-Right
        Surface(
          shape = CircleShape,
          color = Color.Black.copy(alpha = 0.6f),
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(10.dp)
            .size(36.dp)
            .clickable { onSaveClick() }
            .testTag("save_button_${vehicle.id}")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Save Car",
              tint = if (isSaved) AmberGold else Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Bottom info on image: Year & Body type
        Row(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "${vehicle.year} • ${vehicle.bodyType.label}",
            fontSize = 12.sp,
            color = SlateMuted,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Details Block
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "${vehicle.brand} ${vehicle.model}",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = PlatinumText,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Price in Naira and USD
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text(
              text = CurrencyFormatter.formatKoboToNaira(vehicle.priceKobo),
              fontSize = 19.sp,
              fontWeight = FontWeight.Black,
              color = AmberGold
            )
            Text(
              text = "Approx. ${CurrencyFormatter.formatKoboToUSD(vehicle.priceKobo)}",
              fontSize = 11.sp,
              color = SlateMuted
            )
          }

          // Location badge
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = SlateMuted,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = vehicle.location.split(",").firstOrNull() ?: "Lagos",
              fontSize = 11.sp,
              color = SlateMuted,
              maxLines = 1
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Spec pills row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SpecChip(label = "${vehicle.horsepower} HP")
          SpecChip(label = "${vehicle.acceleration0to100}s 0-100")
          SpecChip(label = vehicle.transmission.label.take(9))
          SpecChip(label = "${vehicle.mileageKm} km")
        }
      }
    }
  }
}

@Composable
fun SpecChip(label: String) {
  Surface(
    shape = RoundedCornerShape(6.dp),
    color = Color(0xFF1B283E),
    border = BorderStroke(1.dp, CarbonBorder)
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = PlatinumText,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun StatusBadge(text: String, color: Color) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = color.copy(alpha = 0.15f),
    border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
  ) {
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = color,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

/**
 * Custom Animated GPS Radar & Delivery Route Map Canvas
 */
@Composable
fun LiveGpsRouteMapCanvas(
  progressFraction: Float, // 0.0 (Hub) to 1.0 (Customer Destination)
  carrierHeading: Float,
  speedKmh: Float,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.6f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "radarPulse"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(240.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0xFF070B12))
      .border(BorderStroke(1.dp, CarbonBorder), RoundedCornerShape(16.dp))
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // 1. Draw Grid lines
      val gridSpacing = 40.dp.toPx()
      var x = 0f
      while (x < w) {
        drawLine(
          color = Color(0xFF121B2A),
          start = Offset(x, 0f),
          end = Offset(x, h),
          strokeWidth = 1f
        )
        x += gridSpacing
      }
      var y = 0f
      while (y < h) {
        drawLine(
          color = Color(0xFF121B2A),
          start = Offset(0f, y),
          end = Offset(w, y),
          strokeWidth = 1f
        )
        y += gridSpacing
      }

      // 2. Define Highway Route Points (Lekki Hub -> Toll Gate -> Victoria Island -> Ikoyi)
      val p0 = Offset(w * 0.12f, h * 0.78f) // Hub
      val p1 = Offset(w * 0.35f, h * 0.65f)
      val p2 = Offset(w * 0.60f, h * 0.40f)
      val p3 = Offset(w * 0.88f, h * 0.22f) // Destination

      val path = Path().apply {
        moveTo(p0.x, p0.y)
        cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
      }

      // Highway shadow & glow
      drawPath(
        path = path,
        color = Color(0xFF1E3A5F),
        style = Stroke(width = 10f)
      )
      drawPath(
        path = path,
        color = ElectricCyan.copy(alpha = 0.6f),
        style = Stroke(width = 4f)
      )

      // Start Hub Node
      drawCircle(
        color = Color(0xFFD97706),
        radius = 9.dp.toPx(),
        center = p0
      )
      drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = p0
      )

      // Destination Node
      drawCircle(
        color = EmeraldSuccess,
        radius = 9.dp.toPx(),
        center = p3
      )
      drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = p3
      )

      // Current Carrier Position along curve
      val t = progressFraction.coerceIn(0.05f, 0.95f)
      // Cubic Bezier interpolation
      val u = 1 - t
      val tt = t * t
      val uu = u * u
      val uuu = uu * u
      val ttt = tt * t

      val cx = uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x
      val cy = uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y
      val carrierPos = Offset(cx, cy)

      // Radar Pulse animation
      drawCircle(
        color = AmberGold.copy(alpha = 0.3f),
        radius = 24.dp.toPx() * pulseScale,
        center = carrierPos
      )
      drawCircle(
        color = AmberGold,
        radius = 11.dp.toPx(),
        center = carrierPos
      )
      drawCircle(
        color = Color(0xFF090D16),
        radius = 5.dp.toPx(),
        center = carrierPos
      )
    }

    // Overlay Tag: Telemetry
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = Color(0xCC090D16),
      border = BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Box(
          modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(EmeraldSuccess)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "GPS LIVE • FLATBED TELEMETRY: ${speedKmh.toInt()} KM/H",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = PlatinumText
        )
      }
    }

    // Legend at bottom
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(Color(0xCC070B12))
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFD97706)))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Lekki Showroom Hub", fontSize = 11.sp, color = SlateMuted)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldSuccess))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Your Residence", fontSize = 11.sp, color = PlatinumText, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

private fun Modifier.border(borderStroke: BorderStroke, roundedCornerShape: RoundedCornerShape): Modifier =
  this.then(Modifier.background(Color.Transparent, roundedCornerShape))
