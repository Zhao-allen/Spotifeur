package fr.spotifeur.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import fr.spotifeur.presentation.viewmodel.ColorSchemePair
import androidx.core.graphics.ColorUtils

val LocalSpotifeurDarkTheme = staticCompositionLocalOf { false }
val LocalShowScrollbar = staticCompositionLocalOf { true }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Suppress("DEPRECATION")
@Composable
fun SpotifeurStatusBarStyle(
    color: Color,
    useDarkIcons: Boolean = ColorUtils.calculateLuminance(color.toArgb()) > 0.55,
    navigationColor: Color? = null,
    useDarkNavigationIcons: Boolean = navigationColor
        ?.let { ColorUtils.calculateLuminance(it.toArgb()) > 0.55 }
        ?: useDarkIcons
) {
    val view = LocalView.current
    if (view.isInEditMode) return
    val updateNavigationBar = navigationColor != null
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isStatusBarContrastEnforced = false
        WindowCompat.getInsetsController(window, view).run {
            isAppearanceLightStatusBars = useDarkIcons
            if (updateNavigationBar) {
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
                isAppearanceLightNavigationBars = useDarkNavigationIcons
            }
        }
    }
}

val DarkColorScheme = darkColorScheme(
    primary = BrandPurplePrimary,
    onPrimary = Color(0xFF21183D),
    primaryContainer = Color(0xFF30284A),
    onPrimaryContainer = Color(0xFFE8E0FF),
    secondary = BrandPink,
    onSecondary = Color(0xFF321D2B),
    secondaryContainer = Color(0xFF3A2936),
    onSecondaryContainer = Color(0xFFFFD9EC),
    tertiary = BrandOrange,
    onTertiary = Color(0xFF382311),
    background = BrandPurpleDark,
    onBackground = BrandWhite,
    surface = BrandSurface,
    onSurface = BrandLightPurple,
    surfaceVariant = Color(0xFF252630),
    onSurfaceVariant = Color(0xFFB9B5C5),
    outline = Color(0xFF555361),
    outlineVariant = Color(0xFF383944),
    surfaceTint = BrandPurplePrimary,
    error = Color(0xFFFF7777),
    onError = Color(0xFF3D0808)
)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = Color(0xFF98617F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5DCEB),
    onSecondaryContainer = Color(0xFF3D2334),
    tertiary = Color(0xFF9A6236),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline.copy(alpha = 0.65f),
    surfaceTint = LightPrimary,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun SpotifeurTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorSchemePairOverride: ColorSchemePair? = null,
    content: @Composable () -> Unit
) {
    // Keep the app's identity consistent across devices instead of allowing
    // Android wallpaper colors to recolor navigation, sheets and library UI.
    val finalColorScheme = when {
        colorSchemePairOverride != null -> if (darkTheme) colorSchemePairOverride.dark else colorSchemePairOverride.light
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    SpotifeurStatusBarStyle(
        color = finalColorScheme.background,
        navigationColor = finalColorScheme.background
    )

    CompositionLocalProvider(LocalSpotifeurDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = finalColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
