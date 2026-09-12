package com.yourorg.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yourorg.app.Core.Localization.AppLanguage
import com.yourorg.app.Core.Localization.LocalAppLanguage
import com.yourorg.app.Core.Localization.LocalOnLanguageChange
import com.yourorg.app.Core.Navigation.RootApp
import com.yourorg.app.Core.Theme.AppTheme
import com.yourorg.app.Data.Settings.SettingsStore

@Composable
fun App() {
    var language by remember { mutableStateOf(AppLanguage.fromCode(SettingsStore.language())) }

    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalOnLanguageChange provides { newLanguage ->
            language = newLanguage
            SettingsStore.setLanguage(newLanguage.code)
        }
    ) {
        AppTheme {
            RootApp()
        }
    }
}
