package com.konye.historiq.Core.Theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

    /** The Daily Battle's color, given by the project owner: used on its Play card and in lists, never inside a game. */
    val BattleGold = Color(0xFFE09F3E)

    /** Highlight for a perfect (max-possible) score on any quiz's results screen. */
    val Success = Color(0xFF2E7D32)
}

fun Color.lighten(amount: Float): Color = lerp(this, Color.White, amount)
fun Color.darken(amount: Float): Color = lerp(this, Color.Black, amount)

/** WCAG AA minimum for normal-size text. */
const val MIN_TEXT_CONTRAST = 4.5f

/** WCAG contrast ratio between two opaque colors: 1 (identical) to 21 (black on white). */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance() + 0.05f
    val lb = b.luminance() + 0.05f
    return if (la > lb) la / lb else lb / la
}

/**
 * Cream or espresso-dark, whichever actually contrasts more against [color]. Compared by contrast
 * ratio rather than a luminance threshold: mid-tones like SlateBlue or Tan sit below 0.5 luminance
 * yet read far better with espresso text than with cream.
 */
fun contrastingOn(color: Color): Color =
    if (contrastRatio(AppColors.Cream, color) >= contrastRatio(AppColors.EspressoDark, color)) AppColors.Cream else AppColors.EspressoDark

/**
 * This color faded to [alpha] for de-emphasis, but only as far as it still meets [MIN_TEXT_CONTRAST]
 * over [background] — the alpha is raised just enough where a fixed value would drop below AA.
 */
fun Color.dimmedOn(background: Color, alpha: Float): Color {
    var a = alpha
    while (a < 1f && contrastRatio(copy(alpha = a).compositeOver(background), background) < MIN_TEXT_CONTRAST) a += 0.01f
    return copy(alpha = a.coerceAtMost(1f))
}

/**
 * This color moved toward white or black — whichever takes the smaller shift — only as far as it takes to
 * reach [minContrast] over [background], and unchanged when it already does. For a fixed-hue highlight, like
 * the success green, that has to stay legible on a different game color on every screen.
 */
fun Color.legibleOn(background: Color, minContrast: Float = 3f): Color {
    if (contrastRatio(this, background) >= minContrast) return this
    fun shifted(toward: Color): Pair<Color, Float> {
        var amount = 0f
        var color = this
        while (amount < 1f && contrastRatio(color, background) < minContrast) {
            amount += 0.02f
            color = lerp(this, toward, amount)
        }
        return color to amount
    }
    val (lighter, lighterAmount) = shifted(Color.White)
    val (darker, darkerAmount) = shifted(Color.Black)
    val lighterOk = contrastRatio(lighter, background) >= minContrast
    val darkerOk = contrastRatio(darker, background) >= minContrast
    return when {
        lighterOk && darkerOk -> if (lighterAmount <= darkerAmount) lighter else darker
        lighterOk -> lighter
        darkerOk -> darker
        else -> if (contrastRatio(lighter, background) >= contrastRatio(darker, background)) lighter else darker
    }
}

/**
 * This color with its lightness raised (on a dark [background]) or lowered (on a light one) just until it reaches
 * [minContrast], keeping its hue and saturation, so a red still reads as red and a green as green where [legibleOn]'s
 * blend toward white would turn the error maroon into a dusty pink. Unchanged when it already reads.
 */
fun Color.liftedToRead(background: Color, minContrast: Float = MIN_TEXT_CONTRAST): Color {
    if (contrastRatio(this, background) >= minContrast) return this
    val high = max(red, max(green, blue))
    val low = min(red, min(green, blue))
    val spread = high - low
    val lightness = (high + low) / 2f
    val saturation = if (spread == 0f) 0f else spread / (1f - abs(2f * lightness - 1f))
    val hue = when {
        spread == 0f -> 0f
        high == red -> 60f * (((green - blue) / spread + 6f) % 6f)
        high == green -> 60f * ((blue - red) / spread + 2f)
        else -> 60f * ((red - green) / spread + 4f)
    }
    val step = if (background.luminance() < 0.18f) 0.01f else -0.01f
    var candidate = this
    var level = lightness
    while (level in 0f..1f && contrastRatio(candidate, background) < minContrast) {
        level += step
        candidate = Color.hsl(hue, saturation.coerceIn(0f, 1f), level.coerceIn(0f, 1f))
    }
    return candidate
}

/** The error color as text on this scheme's own background: the raw maroon is only 1-2:1 on the dark game colors. */
fun ColorScheme.errorText(): Color = error.liftedToRead(background)

/** [AppColors.Success] as text on this scheme's own background, for a correct answer. */
fun ColorScheme.successText(): Color = AppColors.Success.liftedToRead(background)

private const val ACCENT_SHIFT = 0.32f
private const val MIN_ACCENT_SHIFT = 0.2f

/**
 * [base] shifted [ACCENT_SHIFT] toward white (dark bases) or black (light ones), eased back toward
 * [base] in small steps only if neither text color would reach [MIN_TEXT_CONTRAST] on the result —
 * OliveDark's full-strength accent lands where cream and espresso both read at ~4.2:1.
 */
private fun readableAccent(base: Color, baseIsDark: Boolean): Color {
    fun shifted(amount: Float) = if (baseIsDark) base.lighten(amount) else base.darken(amount)
    var amount = ACCENT_SHIFT
    while (amount >= MIN_ACCENT_SHIFT) {
        val accent = shifted(amount)
        if (contrastRatio(contrastingOn(accent), accent) >= MIN_TEXT_CONTRAST) return accent
        amount -= 0.01f
    }
    return shifted(ACCENT_SHIFT)
}

/**
 * Builds a full color scheme around one game's signature color: it becomes the screen
 * background, with an accent shade (lightened if the base is dark, darkened if it's light,
 * so it always reads distinctly against the background) driving buttons, inputs and outlines.
 * Every on-color is whichever of cream/espresso contrasts more with the color it sits on.
 */
fun gameColorScheme(base: Color): ColorScheme {
    val baseIsDark = base.luminance() < 0.5f
    val accent = readableAccent(base, baseIsDark)
    val onBase = contrastingOn(base)
    val onAccent = contrastingOn(accent)
    val variant = if (baseIsDark) base.lighten(0.15f) else base.darken(0.15f)
    val scheme = lightColorScheme(
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
    if (onBase != AppColors.Cream) return scheme
    // lightColorScheme's own container roles (dialogs, menus, default cards) stay near-white, which would
    // put this scheme's cream text on white wherever the base is dark.
    return scheme.copy(
        surfaceContainerLowest = base,
        surfaceContainerLow = base,
        surfaceContainer = base,
        surfaceContainerHigh = variant,
        surfaceContainerHighest = variant
    )
}

private val AppLightColors = lightColorScheme(
    primary = AppColors.Tan,
    onPrimary = contrastingOn(AppColors.Tan),
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
 * unaffected: each game's signature color is a fixed brand color, not a light/dark variant. The text
 * styles follow the player's [LocalAppFontSize] on every screen, and reach the screens' own nested
 * MaterialThemes (which inherit the typography) and dialogs. */
@Composable
fun AppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val fontSize = LocalAppFontSize.current
    val typography = remember(fontSize) { Typography().scaledFor(fontSize) }
    MaterialTheme(colorScheme = if (darkTheme) AppDarkColors else AppLightColors, typography = typography, content = content)
}
