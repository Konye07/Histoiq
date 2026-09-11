package com.yourorg.app.Core.Platform

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSettings(name: String): Settings =
    SharedPreferencesSettings(AndroidPlatformContext.app.getSharedPreferences(name, Context.MODE_PRIVATE))
