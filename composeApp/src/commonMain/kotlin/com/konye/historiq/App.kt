package com.konye.historiq

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.konye.historiq.Core.Localization.LocalAppLanguage
import com.konye.historiq.Core.Localization.LocalOnLanguageChange
import com.konye.historiq.Core.Navigation.RootApp
import com.konye.historiq.Core.Platform.ApplyStatusBarStyle
import com.konye.historiq.Core.Theme.AppFontSize
import com.konye.historiq.Core.Theme.AppTheme
import com.konye.historiq.Core.Theme.LocalAppFontSize
import com.konye.historiq.Core.Theme.LocalIsDarkTheme
import com.konye.historiq.Core.Theme.LocalOnDarkModeChange
import com.konye.historiq.Core.Theme.LocalOnFontSizeChange
import com.konye.historiq.Data.Settings.SettingsStore

@Composable
fun App() {
    var language by remember { mutableStateOf(SettingsStore.startingLanguage()) }

    // null = no explicit choice yet, so keep following the system setting live; once the user
    // toggles it in Settings this becomes a sticky override, same as SettingsStore.setLanguage.
    var darkModeOverride by remember { mutableStateOf(SettingsStore.darkModeOverride()) }
    val isDarkTheme = darkModeOverride ?: isSystemInDarkTheme()

    var fontSize by remember { mutableStateOf(AppFontSize.fromCode(SettingsStore.fontSize())) }

    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalOnLanguageChange provides { newLanguage ->
            language = newLanguage
            SettingsStore.setLanguage(newLanguage.code)
        },
        LocalIsDarkTheme provides isDarkTheme,
        LocalOnDarkModeChange provides { newValue ->
            darkModeOverride = newValue
            SettingsStore.setDarkModeOverride(newValue)
        },
        LocalAppFontSize provides fontSize,
        LocalOnFontSizeChange provides { newSize ->
            fontSize = newSize
            SettingsStore.setFontSize(newSize.code)
        }
    ) {
        ApplyStatusBarStyle(isDarkTheme)
        AppTheme(darkTheme = isDarkTheme) {
            RootApp()
        }
    }
}
