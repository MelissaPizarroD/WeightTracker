package com.isoft.weighttracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = OnGreenPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = OnGreenPrimaryContainer,

    secondary = GreenSecondary,
    onSecondary = OnGreenSecondary,
    secondaryContainer = GreenSecondaryContainer,
    onSecondaryContainer = OnGreenSecondaryContainer,

    tertiary = BlueTertiary,
    onTertiary = OnBlueTertiary,
    tertiaryContainer = BlueTertiaryContainer,
    onTertiaryContainer = OnBlueTertiaryContainer,

    background = AppBackground,
    onBackground = OnBackground,
    surface = AppSurface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    error = AppError,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = OnGreenPrimary,
    primaryContainer = Color(0xFF145C45),
    onPrimaryContainer = GreenPrimaryContainer,

    secondary = GreenSecondary,
    onSecondary = OnGreenSecondary,
    secondaryContainer = Color(0xFF22564C),
    onSecondaryContainer = GreenSecondaryContainer,

    tertiary = BlueTertiary,
    onTertiary = OnBlueTertiary,
    tertiaryContainer = Color(0xFF0A3B4F),
    onTertiaryContainer = BlueTertiaryContainer,

    background = Color(0xFF0F1A15),
    onBackground = Color(0xFFE1EDE7),
    surface = Color(0xFF15201B),
    onSurface = Color(0xFFE1EDE7),
    surfaceVariant = Color(0xFF37453F),
    onSurfaceVariant = OnSurfaceVariant,

    error = AppError,
    onError = OnError,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = ErrorContainer
)

@Composable
fun WeightTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
