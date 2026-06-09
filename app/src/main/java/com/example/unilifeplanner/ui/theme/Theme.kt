package com.example.unilifeplanner.ui.theme

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color.TRANSPARENT
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.unilifeplanner.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = UniLifeBlue,
    onPrimary = Color.White,
    primaryContainer = UniLifeBlueContainer,
    onPrimaryContainer = Color(0xFF001B3F),
    secondary = UniLifeTeal,
    onSecondary = Color.White,
    secondaryContainer = UniLifeTealContainer,
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = UniLifeAmber,
    onTertiary = Color.White,
    tertiaryContainer = UniLifeAmberContainer,
    onTertiaryContainer = Color(0xFF261A00),
    background = UniLifeBackgroundLight,
    onBackground = UniLifeOnSurfaceLight,
    surface = UniLifeSurfaceLight,
    onSurface = UniLifeOnSurfaceLight,
    surfaceVariant = UniLifeSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF3F4857),
    outline = UniLifeOutlineLight,
    error = UniLifeError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = UniLifeBlueDark,
    onPrimary = Color(0xFF003063),
    primaryContainer = UniLifeBlueDarkContainer,
    onPrimaryContainer = UniLifeBlueContainer,
    secondary = UniLifeTealDark,
    onSecondary = Color(0xFF003732),
    secondaryContainer = UniLifeTealDarkContainer,
    onSecondaryContainer = UniLifeTealContainer,
    tertiary = UniLifeAmberDark,
    onTertiary = Color(0xFF4A3700),
    tertiaryContainer = UniLifeAmberDarkContainer,
    onTertiaryContainer = UniLifeAmberContainer,
    background = UniLifeBackgroundDark,
    onBackground = UniLifeOnSurfaceDark,
    surface = UniLifeSurfaceDark,
    onSurface = UniLifeOnSurfaceDark,
    surfaceVariant = UniLifeSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC5CEDC),
    outline = UniLifeOutlineDark,
    error = UniLifeErrorDark,
    onError = Color(0xFF690005)
)

@Composable
fun UniLifePlannerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    ApplySystemBarsStyle(
        darkTheme = darkTheme,
        colorScheme = colorScheme
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = UniLifeShapes,
        content = content
    )
}

@Composable
private fun ApplySystemBarsStyle(
    darkTheme: Boolean,
    colorScheme: ColorScheme
) {
    val context = LocalContext.current
    SideEffect {
        val activity = context.findActivity() ?: return@SideEffect
        val statusBarStyle = if (darkTheme) {
            SystemBarStyle.dark(TRANSPARENT)
        } else {
            SystemBarStyle.light(TRANSPARENT, TRANSPARENT)
        }
        val navigationBarStyle = if (darkTheme) {
            SystemBarStyle.dark(TRANSPARENT)
        } else {
            SystemBarStyle.light(TRANSPARENT, TRANSPARENT)
        }

        activity.enableEdgeToEdge(
            statusBarStyle = statusBarStyle,
            navigationBarStyle = navigationBarStyle
        )
    }
}

private tailrec fun Context.findActivity(): ComponentActivity? =
    when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
