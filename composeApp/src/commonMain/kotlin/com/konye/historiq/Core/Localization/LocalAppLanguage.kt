package com.konye.historiq.Core.Localization

import androidx.compose.runtime.compositionLocalOf

/**
 * Deliberately not backed by Compose Multiplatform's resource-system locale resolution: that
 * system doesn't reliably support runtime language switching on iOS (JetBrains/compose-
 * multiplatform's own tracker, CMP-8376) since it resolves against the OS/system locale, not an
 * explicit in-app override — which is exactly what this app's language picker needs (switch
 * independent of system locale, on both platforms, with no restart). `compositionLocalOf` (not
 * `staticCompositionLocalOf`) because the value is expected to change at runtime and every
 * string-reading composable needs to recompose when it does.
 */
val LocalAppLanguage = compositionLocalOf { AppLanguage.EN }

/**
 * Paired with [LocalAppLanguage] at the same provider site (see `App.kt`) so a deeply nested
 * composable (the language picker in `SettingsScreen`) can change the app-wide language without
 * threading a callback parameter through every intermediate screen/nav-host signature.
 */
val LocalOnLanguageChange = compositionLocalOf<(AppLanguage) -> Unit> { {} }
