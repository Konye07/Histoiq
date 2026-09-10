package com.yourorg.app.Core.Locale

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

/**
 * Per-app language override, independent of the device's system locale — the app stores its own
 * language preference (SettingsStore) and applies it by wrapping the Activity's base Context with
 * a Configuration pinned to that locale, since the app has no AppCompat base to use
 * AppCompatDelegate.setApplicationLocales() and minSdk 26 predates the platform per-app language API.
 */
object LocaleHelper {
    fun wrap(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
