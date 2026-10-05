package com.konye.historiq.Core.Theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified

/**
 * The player's text size choice, applied on top of whatever text size the device itself is set to
 * ([scale] multiplies it). [MEDIUM] is the size the app always had, and the default.
 */
enum class AppFontSize(val code: String, val scale: Float) {
    SMALL("small", 0.85f),
    MEDIUM("medium", 1f),
    LARGE("large", 1.25f);

    companion object {
        fun fromCode(code: String?): AppFontSize = entries.find { it.code == code } ?: MEDIUM
    }
}

/**
 * The app's current text size — already resolved by the time anything reads it, like [LocalIsDarkTheme].
 * Provided together with [LocalOnFontSizeChange] in `App.kt`, so Settings can change it without a callback
 * threaded through every screen.
 */
val LocalAppFontSize = compositionLocalOf { AppFontSize.MEDIUM }

val LocalOnFontSizeChange = compositionLocalOf<(AppFontSize) -> Unit> { {} }

/**
 * Every text style at [size]: [AppTheme] hands this to MaterialTheme, so all the app's text follows. It is the
 * styles that are scaled, not the density's fontScale, because dialogs get a window of their own with a fresh
 * density (a density override would leave them at the old size), while the theme reaches them like any other
 * composition local. Text must therefore take its size from MaterialTheme.typography, never a literal sp.
 */
fun Typography.scaledFor(size: AppFontSize): Typography = if (size == AppFontSize.MEDIUM) this else copy(
    displayLarge = displayLarge.scaledBy(size.scale),
    displayMedium = displayMedium.scaledBy(size.scale),
    displaySmall = displaySmall.scaledBy(size.scale),
    headlineLarge = headlineLarge.scaledBy(size.scale),
    headlineMedium = headlineMedium.scaledBy(size.scale),
    headlineSmall = headlineSmall.scaledBy(size.scale),
    titleLarge = titleLarge.scaledBy(size.scale),
    titleMedium = titleMedium.scaledBy(size.scale),
    titleSmall = titleSmall.scaledBy(size.scale),
    bodyLarge = bodyLarge.scaledBy(size.scale),
    bodyMedium = bodyMedium.scaledBy(size.scale),
    bodySmall = bodySmall.scaledBy(size.scale),
    labelLarge = labelLarge.scaledBy(size.scale),
    labelMedium = labelMedium.scaledBy(size.scale),
    labelSmall = labelSmall.scaledBy(size.scale)
)

/** Size, line height and letter spacing all scaled together, so the text keeps its proportions. */
fun TextStyle.scaledBy(factor: Float): TextStyle = copy(
    fontSize = fontSize.scaledBy(factor),
    lineHeight = lineHeight.scaledBy(factor),
    letterSpacing = letterSpacing.scaledBy(factor)
)

private fun TextUnit.scaledBy(factor: Float): TextUnit = if (isSpecified) this * factor else this
