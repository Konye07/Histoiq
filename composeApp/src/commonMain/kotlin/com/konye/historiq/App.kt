package com.konye.historiq

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.konye.historiq.Core.Localization.AppLanguage
import com.konye.historiq.Core.Localization.LocalAppLanguage
import com.konye.historiq.Core.Localization.LocalOnLanguageChange
import com.konye.historiq.Core.Navigation.RootApp
import com.konye.historiq.Core.Theme.AppTheme
import com.konye.historiq.Data.Settings.SettingsStore

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
