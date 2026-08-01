// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/ui/theme/Theme.kt
package com.geosurvey.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryTealDark,
    onPrimaryContainer = TextPrimary,
    secondary = AccentCyan,
    onSecondary = TextPrimary,
    secondaryContainer = PrimaryTealDark,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentCyanLight,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = BackgroundCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGlass,
    onSurfaceVariant = TextSecondary,
    error = StatusPoor,
    onError = TextPrimary,
    outline = TextMuted
)

@Composable
fun GeoSurveyTheme(
    darkTheme: Boolean = true, // 地质软件默认暗色模式
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
