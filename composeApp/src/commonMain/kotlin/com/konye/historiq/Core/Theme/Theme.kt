package com.konye.historiq.Core.Theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** The app's fixed historical palette — used regardless of system light/dark setting. */
object AppColors {
    val Maroon = Color(0xFF6B1C16)
    val Rust = Color(0xFF8E372F)
    val SlateBlue = Color(0xFF788C9E)
    val OliveDark = Color(0xFF44422D)
    val BrownDark = Color(0xFF443223)
    val Mauve = Color(0xFF755151)
    val Tan = Color(0xFFA08670)
    val Cream = Color(0xFFFFF9F3)
    val ForestDark = Color(0xFF1E2A20)
    val EspressoDark = Color(0xFF201409)

    /** Highlight for a perfect (max-possible) score on any quiz's results screen. */
    val Success = Color(0xFF2E7D32)
}

fun Color.lighten(amount: Float): Color = lerp(this, Color.White, amount)
fun Color.darken(amount: Float): Color = lerp(this, Color.Black, amount)

/** Cream on a dark color, espresso-dark on a light one — always legible either way. */
fun contrastingOn(color: Color): Color = if (color.luminance() < 0.5f) AppColors.Cream else AppColors.EspressoDark

/**
 * Builds a full color scheme around one game's signature color: it becomes the screen
 * background, with an accent shade (lightened if the base is dark, darkened if it's light,
 * so it always reads distinctly against the background) driving buttons, inputs and outlines.
 */
fun gameColorScheme(base: Color): ColorScheme {
    val baseIsDark = base.luminance() < 0.5f
    val accent = if (baseIsDark) base.lighten(0.32f) else base.darken(0.32f)
    val onBase = contrastingOn(base)
    val onAccent = contrastingOn(accent)
    val variant = if (baseIsDark) base.lighten(0.15f) else base.darken(0.15f)
    return lightColorScheme(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accent,
        onPrimaryContainer = onAccent,
        secondary = accent,
        onSecondary = onAccent,
        background = base,
        onBackground = onBase,
        surface = base,
        onSurface = onBase,
        surfaceVariant = variant,
        onSurfaceVariant = onBase,
        outline = accent,
        error = AppColors.Maroon,
        onError = AppColors.Cream
    )
}

private val AppLightColors = lightColorScheme(
    primary = AppColors.Tan,
    onPrimary = AppColors.Cream,
    primaryContainer = AppColors.Tan.lighten(0.35f),
    onPrimaryContainer = AppColors.EspressoDark,
    secondary = AppColors.Mauve,
    onSecondary = AppColors.Cream,
    background = AppColors.Cream,
    onBackground = AppColors.EspressoDark,
    surface = AppColors.Cream,
    onSurface = AppColors.EspressoDark,
    surfaceVariant = AppColors.Tan.lighten(0.55f),
    onSurfaceVariant = AppColors.EspressoDark,
    outline = AppColors.Mauve,
    error = AppColors.Maroon,
    onError = AppColors.Cream
)

/** Same palette as [AppLightColors], same roles — just the dark end of each color used for
 * surfaces and the light end for content, instead of a separately invented dark palette. */
private val AppDarkColors = darkColorScheme(
    primary = AppColors.Tan,
    onPrimary = AppColors.EspressoDark,
    primaryContainer = AppColors.Tan.darken(0.45f),
    onPrimaryContainer = AppColors.Cream,
    secondary = AppColors.Mauve.lighten(0.2f),
    onSecondary = AppColors.EspressoDark,
    background = AppColors.EspressoDark,
    onBackground = AppColors.Cream,
    surface = AppColors.EspressoDark,
    onSurface = AppColors.Cream,
    surfaceVariant = AppColors.EspressoDark.lighten(0.18f),
    onSurfaceVariant = AppColors.Cream,
    outline = AppColors.Mauve.lighten(0.2f),
    error = AppColors.Rust,
    onError = AppColors.Cream
)

/** [darkTheme] picks between [AppLightColors]/[AppDarkColors] — only for the non-game screens
 * (Play hub, Friends, Profile, Settings). Game screens use [gameColorScheme] instead, which is
 * unaffected: each game's signature color is a fixed brand color, not a light/dark variant. */
@Composable
fun AppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) AppDarkColors else AppLightColors, content = content)
}
