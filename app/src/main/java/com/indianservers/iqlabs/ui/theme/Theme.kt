package com.indianservers.iqlabs.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppColorTheme(val label: String) {
    SimpleWhite("Simple White"),
    LabDark("Lab Dark"),
    Ocean("Ocean"),
    Forest("Forest"),
    Sunset("Sunset"),
}

private val LabDarkColorScheme = darkColorScheme(
    primary = LabCyan,
    secondary = LabViolet,
    tertiary = LabLime,
    background = LabNavy,
    surface = LabPanel,
    surfaceVariant = LabNavy2,
    onPrimary = LabInk,
    onSecondary = Color.White,
    onTertiary = LabInk,
    onBackground = Color(0xFFF3FAFF),
    onSurface = Color(0xFFF3FAFF),
)

private val SimpleWhiteColorScheme = lightColorScheme(
    primary = Color(0xFF006B85),
    secondary = Color(0xFF6750A4),
    tertiary = Color(0xFF4E7A19),
    background = Color(0xFFFBFCFE),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F4F8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LabInk,
    onSurface = LabInk,
)

private val OceanColorScheme = lightColorScheme(
    primary = Color(0xFF00758F),
    secondary = Color(0xFF2263A5),
    tertiary = Color(0xFF1C7C74),
    background = Color(0xFFF5FBFF),
    surface = Color.White,
    surfaceVariant = Color(0xFFE4F3FA),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF082430),
    onSurface = Color(0xFF082430),
)

private val ForestColorScheme = lightColorScheme(
    primary = Color(0xFF2F6F3E),
    secondary = Color(0xFF5B6F2F),
    tertiary = Color(0xFF00796B),
    background = Color(0xFFFAFCF7),
    surface = Color.White,
    surfaceVariant = Color(0xFFEAF3E3),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF152114),
    onSurface = Color(0xFF152114),
)

private val SunsetColorScheme = lightColorScheme(
    primary = Color(0xFFB44928),
    secondary = Color(0xFF8B4C9E),
    tertiary = Color(0xFF9A6500),
    background = Color(0xFFFFFBF8),
    surface = Color.White,
    surfaceVariant = Color(0xFFFFECE1),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2A1B16),
    onSurface = Color(0xFF2A1B16),
)

@Composable
fun IQLabsTheme(
    appColorTheme: AppColorTheme = AppColorTheme.SimpleWhite,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appColorTheme) {
        AppColorTheme.SimpleWhite -> SimpleWhiteColorScheme
        AppColorTheme.LabDark -> LabDarkColorScheme
        AppColorTheme.Ocean -> OceanColorScheme
        AppColorTheme.Forest -> ForestColorScheme
        AppColorTheme.Sunset -> SunsetColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
