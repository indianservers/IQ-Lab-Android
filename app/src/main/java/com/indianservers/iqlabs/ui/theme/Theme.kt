package com.indianservers.iqlabs.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val IqShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

enum class AppColorTheme(val label: String) {
    SimpleWhite("Light"),
    LabDark("Dark"),
    Ocean("Ocean"),
    Forest("Forest"),
    Sunset("Sunset"),
}

data class IqExtras(
    val isDark: Boolean,
    val card: Color,
    val muted: Color,
    val navBar: Color,
    val navContent: Color,
    val navSelected: Color,
    val chipIdle: Color,
    val heroScrim: Brush,
    val pageBrush: Brush,
)

val LocalIqExtras = staticCompositionLocalOf {
    IqExtras(
        isDark = false,
        card = Color.White,
        muted = IqMuted,
        navBar = Color.White,
        navContent = IqNavDark,
        navSelected = IqPurple,
        chipIdle = Color.White,
        heroScrim = Brush.verticalGradient(listOf(Color.Transparent, Color.White)),
        pageBrush = Brush.verticalGradient(listOf(LabMist, Color.White)),
    )
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
    primary = IqPurple,
    secondary = IqSky,
    tertiary = IqPeach,
    background = LabMist,
    surface = Color.White,
    surfaceVariant = IqSoftBlue,
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
    background = Color(0xFFEEF8FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFD7F0FA),
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
    background = Color(0xFFF3F8EE),
    surface = Color.White,
    surfaceVariant = Color(0xFFE4F1DA),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF152114),
    onSurface = Color(0xFF152114),
)

private val SunsetColorScheme = lightColorScheme(
    primary = Color(0xFFC24B2C),
    secondary = Color(0xFF8B4C9E),
    tertiary = Color(0xFF9A6500),
    background = Color(0xFFFFF6F0),
    surface = Color.White,
    surfaceVariant = Color(0xFFFFE6D8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2A1B16),
    onSurface = Color(0xFF2A1B16),
)

private fun extrasFor(theme: AppColorTheme): IqExtras = when (theme) {
    AppColorTheme.SimpleWhite -> IqExtras(
        isDark = false,
        card = Color.White,
        muted = IqMuted,
        navBar = Color.White,
        navContent = Color(0xFF8A93A8),
        navSelected = IqPurple,
        chipIdle = Color.White,
        heroScrim = Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = .2f))),
        pageBrush = Brush.verticalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFF), Color.White)),
    )
    AppColorTheme.LabDark -> IqExtras(
        isDark = true,
        card = Color(0xFF12243C),
        muted = Color(0xFF9BB0C8),
        navBar = Color(0xFF0C1A2E),
        navContent = Color(0xFF8AA0B8),
        navSelected = LabCyan,
        chipIdle = Color(0xFF163049),
        heroScrim = Brush.verticalGradient(listOf(Color.Transparent, LabNavy)),
        pageBrush = Brush.verticalGradient(listOf(Color(0xFF07111F), Color(0xFF0B1B31))),
    )
    AppColorTheme.Ocean -> IqExtras(
        isDark = false,
        card = Color.White,
        muted = Color(0xFF4E7384),
        navBar = Color.White,
        navContent = Color(0xFF7A97A6),
        navSelected = Color(0xFF00758F),
        chipIdle = Color.White,
        heroScrim = Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = .15f))),
        pageBrush = Brush.verticalGradient(listOf(Color(0xFFDFF4FF), Color(0xFFF5FBFF))),
    )
    AppColorTheme.Forest -> IqExtras(
        isDark = false,
        card = Color.White,
        muted = Color(0xFF5A6B58),
        navBar = Color.White,
        navContent = Color(0xFF8A9A86),
        navSelected = Color(0xFF2F6F3E),
        chipIdle = Color.White,
        heroScrim = Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = .15f))),
        pageBrush = Brush.verticalGradient(listOf(Color(0xFFE7F3DC), Color(0xFFFAFCF7))),
    )
    AppColorTheme.Sunset -> IqExtras(
        isDark = false,
        card = Color.White,
        muted = Color(0xFF8A675C),
        navBar = Color.White,
        navContent = Color(0xFFB3948A),
        navSelected = Color(0xFFC24B2C),
        chipIdle = Color.White,
        heroScrim = Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = .15f))),
        pageBrush = Brush.verticalGradient(listOf(Color(0xFFFFE8D7), Color(0xFFFFFBF8))),
    )
}

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

    CompositionLocalProvider(LocalIqExtras provides extrasFor(appColorTheme)) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = IqShapes,
            content = content
        )
    }
}
