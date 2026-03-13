package com.thebluealliance.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * TBA brand colors as defined in the style guide.
 */
object TBAColors {
    val TBABlue = Color(0xFF3F51B5)           // Indigo 500
    val TBABlueDark = Color(0xFF303F9F)       // Indigo 700
    val TBABlueDarker = Color(0xFF1A237E)     // Indigo 900
    val TBABlueLight = Color(0xFF9FA8DA)      // Indigo 200
    val TBAPastelBlue = Color(0xFFC5CAE9)     // Indigo 100
    val TBAIndigo400 = Color(0xFF5C6BC0)      // Indigo 400
    val TBARed = Color(0xFF770000)            // Debug/beta
    val TBARedDark = Color(0xFF440000)        // Debug/beta dark

    // Other UI colors
    val RPInactive = Color(0xFF9CA3AF)
    val FrcBlue = Color(0xFF0066B3)
    val FrcRed = Color(0xFFED1C24)

    val AllianceRed = Color(0XFFF44336)     // From TBA Web Beta
    val AllianceBlue = Color(0XFF007CFF)    // From TBA Web Beta
}

private val LightColorScheme = lightColorScheme(
    primary = TBAColors.TBABlue,
    onPrimary = Color.White,
    primaryContainer = TBAColors.TBAPastelBlue,
    onPrimaryContainer = TBAColors.TBABlueDarker,
    surfaceTint = TBAColors.TBABlue,
)

private val DarkColorScheme = darkColorScheme(
    primary = TBAColors.TBABlueLight,
    onPrimary = Color(0xFF00174D),
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

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
