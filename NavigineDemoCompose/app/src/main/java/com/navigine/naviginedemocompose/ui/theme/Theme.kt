package com.navigine.naviginedemocompose.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext



@Composable
fun NavigineDemoComposeTheme(
    darkTheme: Boolean = false//isSystemInDarkTheme()
    ,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> darkColors()
      else -> lightColors()
    }

    val extended = if (darkTheme) {
        ExtendedColors(
            success = Success,
            successContainer = SuccessContainer.copy(alpha = 0.5f)
        )
    } else {
        ExtendedColors(
            success = Success,
            successContainer = SuccessContainer
        )
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extended,
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }

}

/**
 * Convenient accessor for extended colors:
 * MaterialTheme.extendedColors.success / successContainer
 */
val MaterialTheme.extendedColors: ExtendedColors
    @Composable get() = LocalExtendedColors.current

