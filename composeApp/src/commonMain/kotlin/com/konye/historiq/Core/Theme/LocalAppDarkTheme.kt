package com.konye.historiq.Core.Theme

import androidx.compose.runtime.compositionLocalOf

/**
 * The app's *effective* dark/light state — already resolved (system-detected, or the user's
 * explicit override from Settings) by the time anything reads it, so call sites never need to
 * know which of those two it came from. `compositionLocalOf` (not `staticCompositionLocalOf`)
 * since it changes at runtime, same reasoning as `LocalAppLanguage`.
 */
val LocalIsDarkTheme = compositionLocalOf { false }

/**
 * Paired with [LocalIsDarkTheme] at the same provider site (see `App.kt`) so the toggle deep in
 * `SettingsScreen` can set an explicit override without threading a callback through every
 * intermediate screen/nav-host signature — mirrors `LocalOnLanguageChange`.
 */
val LocalOnDarkModeChange = compositionLocalOf<(Boolean) -> Unit> { {} }
