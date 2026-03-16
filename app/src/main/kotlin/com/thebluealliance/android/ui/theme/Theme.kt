package com.thebluealliance.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * TBA brand colors as defined in the style guide.
 * Grouped into a single object for reusable imports across the project.
 */
object TBAColors {
    val TBABlue = Color(0xFF3F51B5)           // Indigo 500 — canonical TBA brand color
    val TBABlueDark = Color(0xFF303F9F)       // Indigo 700 — dark-mode container
    val TBAIndigo900 = Color(0xFF1A237E)      // Indigo 900 — onPrimaryContainer
    val TBABlueLight = Color(0xFF9FA8DA)      // Indigo 200 — dark-mode primary
    val TBAPastelBlue = Color(0xFFC5CAE9)     // Indigo 100 — light-mode primaryContainer
    val TBAIndigo400 = Color(0xFF5C6BC0)      // Indigo 400 — section headers
    val TBARed = Color(0xFF770000)            // Debug/beta builds
    val TBARedDark = Color(0xFF440000)        // Darker variant

    // Other UI colors
    val RPInactive = Color(0xFF9CA3AF)
    val FrcBlue = Color(0xFF0066B3)
    val FrcRed = Color(0xFFED1C24)

    val AllianceRed = Color(0xFFF44336)     // From TBA Web Beta
    val AllianceRedDark = Color(0xBFF44336)     // From TBA Web Beta
    val AllianceBlue = Color(0xFF007CFF)    // From TBA Web Beta

    val OnPrimaryDark = Color(0xFF00174D)
}

/**
 * Custom alliance colors that can be provided via CompositionLocal.
 */
@Immutable
data class AllianceColors(
    val red: Color,
    val blue: Color
)

/**
 * CompositionLocal to provide theme-aware alliance colors.
 * This allows screens like MatchDetail to automatically switch between accessible red values
 * based on the active theme mode without manual detection.
 */
val LocalAllianceColors = staticCompositionLocalOf {
    AllianceColors(
        red = TBAColors.AllianceRed,
        blue = TBAColors.AllianceBlue
    )
}

private val LightColorScheme = lightColorScheme(
    primary = TBAColors.TBABlue,
    onPrimary = Color.White,
    primaryContainer = TBAColors.TBAPastelBlue,
    onPrimaryContainer = TBAColors.TBAIndigo900,
    surfaceTint = TBAColors.TBABlue,
)

private val DarkColorScheme = darkColorScheme(
    primary = TBAColors.TBABlueLight,
    onPrimary = TBAColors.OnPrimaryDark,
    primaryContainer = TBAColors.TBABlueDark,
    onPrimaryContainer = TBAColors.TBAPastelBlue,
    surfaceTint = TBAColors.TBABlueLight,
)

@Composable
fun TBATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Select alliance colors based on the theme.
    // Use AllianceBlue for both. Red is TBARed in light mode and AllianceRed in dark mode.
    val allianceColors = if (darkTheme) {
        AllianceColors(red = TBAColors.AllianceRedDark, blue = TBAColors.AllianceBlue)
    } else {
        AllianceColors(red = TBAColors.TBARed, blue = TBAColors.AllianceBlue)
    }

    CompositionLocalProvider(LocalAllianceColors provides allianceColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
