package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = AmberGold,
    onPrimary = Color(0xFF1E1100),
    primaryContainer = AmberGoldDark,
    onPrimaryContainer = AmberGoldLight,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF00253B),
    secondaryContainer = Color(0xFF0C3B5E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = CarbonDark,
    onBackground = PlatinumText,
    surface = CarbonCard,
    onSurface = PlatinumText,
    surfaceVariant = CarbonCardElevated,
    onSurfaceVariant = SlateMuted,
    outline = CarbonBorder,
    error = CrimsonAlert,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AmberGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = PearlWhite,
    onBackground = DarkText,
    surface = LightSurface,
    onSurface = DarkText,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = SlateDark,
    outline = LightBorder,
    error = CrimsonAlert,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Luxury automotive defaults to rich dark aesthetic
  dynamicColor: Boolean = false, // Keep consistent luxury automotive branding
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

