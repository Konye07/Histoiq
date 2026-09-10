package com.yourorg.app.Shared

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.yourorg.app.Core.Locale.LocaleHelper
import com.yourorg.app.Core.Navigation.RootApp
import com.yourorg.app.Core.Theme.AppTheme
import com.yourorg.app.Data.Settings.SettingsStore

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase, SettingsStore.language(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                RootApp()
            }
        }
    }
}
