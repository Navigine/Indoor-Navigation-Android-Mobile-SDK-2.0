package com.navigine.naviginedemocompose.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


// Primary
val BluePrimary = Color(0xFF30AAD9)
val BluePrimary70 = Color(0xB330AAD9) // ~70% alpha
val BluePrimary50 = Color(0x8030AAD9) // ~50% alpha
val BluePrimaryLight = Color(0xFFD2EFFB)

// Secondary
val Secondary = Color(0xFFEAEEF1)
val SecondaryVariant = Color(0xFF96A8AF)

// Tertiary
val Tertiary = Color(0xFFD5E3EE)

// Background / Surface
val Background = Color(0xFFF5F8FB)
val OnBackgroundWhite = Color(0xFFFFFFFF)
val OnBackgroundWhite80 = Color(0xCCFFFFFF)
val OnBackgroundWhite50 = Color(0x80FFFFFF)

// Text
val TextPrimary = Color(0xFF1F1F1F)
val TextOnPrimary = Color(0xFFFFFFFF)
val TextSecondaryGray = Color(0xFF79858A)

// Conditions
val Success = Color(0xFF4CD964)
val SuccessContainer = Color(0x404CD964) // ARGB with alpha
val Error = Color(0xFFFF5758)
val ErrorContainer = Color(0xCCFF5758) // ARGB with alpha

// ----- Material 3 light & dark color schemes -----

/**
 * Light color scheme mapped from tokens.
 * Choose legible on-colors to ensure good contrast on light backgrounds.
 */
fun lightColors() = androidx.compose.material3.lightColorScheme(
    primary = BluePrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = BluePrimaryLight,
    onPrimaryContainer = TextPrimary,

    secondary = SecondaryVariant,
    onSecondary = TextPrimary,
    secondaryContainer = Secondary,
    onSecondaryContainer = TextPrimary,

    tertiary = Tertiary,
    onTertiary = TextPrimary,
    tertiaryContainer = Tertiary,
    onTertiaryContainer = TextPrimary,

    background = Background,
    onBackground = TextPrimary,
    surface = OnBackgroundWhite,
    onSurface = TextPrimary,
    surfaceVariant = Secondary,
    onSurfaceVariant = TextSecondaryGray,

    error = Error,
    onError = TextOnPrimary,
    errorContainer = ErrorContainer,
    onErrorContainer = TextPrimary,

    outline = TextSecondaryGray,
    outlineVariant = Secondary
)

/**
 * Dark color scheme tuned for readability on dark surfaces.
 * We keep the same brand primary and switch core surfaces to dark.
 */
fun darkColors() = androidx.compose.material3.darkColorScheme(
    primary = BluePrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = BluePrimary70,
    onPrimaryContainer = OnBackgroundWhite,

    secondary = SecondaryVariant,
    onSecondary = OnBackgroundWhite,
    secondaryContainer = Color(0xFF2B3337),
    onSecondaryContainer = OnBackgroundWhite,

    tertiary = Tertiary,
    onTertiary = OnBackgroundWhite,
    tertiaryContainer = Color(0xFF283038),
    onTertiaryContainer = OnBackgroundWhite,

    background = Color(0xFF121212),
    onBackground = OnBackgroundWhite,
    surface = Color(0xFF1C1C1E),
    onSurface = OnBackgroundWhite,
    surfaceVariant = Color(0xFF292A2E),
    onSurfaceVariant = OnBackgroundWhite80,

    error = Error,
    onError = OnBackgroundWhite,
    errorContainer = Color(0x66FF5758),
    onErrorContainer = OnBackgroundWhite,

    outline = Color(0xFF5C5F63),
    outlineVariant = Color(0xFF3A3E42)
)

// ----- Extended colors (non-M3 roles) -----

/**
 * Colors that aren't a part of the standard M3 color roles
 * but are useful across the app (e.g., success states).
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = Success,
        successContainer = SuccessContainer
    )
}