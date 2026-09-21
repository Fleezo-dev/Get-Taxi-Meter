package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MeterColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    primaryContainer = BrandRedLight,
    onPrimaryContainer = BrandRedDark,
    secondary = BrandYellow,
    onSecondary = Color.Black,
    secondaryContainer = BrandYellowDark,
    onSecondaryContainer = Color.Black,
    tertiary = MeterGreen,
    onTertiary = Color.White,
    background = AppWhiteBg,
    onBackground = TextPrimary,
    surface = AppCardBg,
    onSurface = TextPrimary,
    surfaceVariant = AppCardSecondary,
    onSurfaceVariant = TextSecondary,
    outline = AppCardBorder
)

@Composable
fun GetTaxiMeterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MeterColorScheme,
        typography = Typography,
        content = content
    )
}

