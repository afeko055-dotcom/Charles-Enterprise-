package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.PlatinumText
import com.example.ui.theme.SlateMuted
import kotlinx.coroutines.delay

@Composable
fun LaunchScreen(
  onLaunchComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var progress by remember { mutableFloatStateOf(0f) }
  var isLoaded by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val heroImageRes = remember {
    val id = context.resources.getIdentifier("img_hero_car", "drawable", context.packageName)
    if (id != 0) id else 0
  }

  // Smooth loading progression
  LaunchedEffect(Unit) {
    delay(200)
    progress = 0.4f
    delay(400)
    progress = 0.8f
    delay(300)
    progress = 1.0f
    delay(200)
    isLoaded = true
    delay(300)
    onLaunchComplete()
  }

  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
    label = "launch_progress"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonDark)
      .clickable { onLaunchComplete() }
      .testTag("launch_screen")
  ) {
    // Subtle background automotive backdrop
    if (heroImageRes != 0) {
      Image(
        painter = painterResource(id = heroImageRes),
        contentDescription = "Charles Enterprise Heritage",
        modifier = Modifier
          .fillMaxSize()
          .alpha(0.18f),
        contentScale = androidx.compose.ui.layout.ContentScale.Crop
      )
    }

    // Vignette
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0xCC090D16), Color(0xFF090D16)),
            radius = 900f
          )
        )
    )

    // Main Brand Architecture
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Emblem Crest
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = AmberGold.copy(alpha = 0.12f),
        border = BorderStroke(1.5.dp, AmberGold),
        modifier = Modifier.size(76.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = "Charles Enterprise Emblem",
            tint = AmberGold,
            modifier = Modifier.size(44.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "CHARLES",
        fontSize = 30.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 6.sp,
        color = PlatinumText
      )

      Text(
        text = "ENTERPRISE",
        fontSize = 20.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = 6.sp,
        color = AmberGold
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "PREMIER AUTOMOBILE VAULT & CONCIERGE",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp,
        color = SlateMuted,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(48.dp))

      // Restrained progress bar
      Column(
        modifier = Modifier.width(220.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(1.5.dp)),
          color = AmberGold,
          trackColor = Color(0xFF1B283E)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = if (isLoaded) "VAULT READY" else "INITIALIZING ENCRYPTED REPOSITORIES...",
          fontSize = 9.sp,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          letterSpacing = 1.sp,
          color = SlateMuted
        )
      }
    }

    // Bottom Escrow & Security Trust Seal
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 36.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Security,
        contentDescription = null,
        tint = AmberGold,
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "AUTHENTICATED ESCROW & WHITE-GLOVE FLATBED DISPATCH",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = SlateMuted
      )
    }
  }
}
