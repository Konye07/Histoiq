package com.konye.historiq.Core.Platform

import com.russhwolf.settings.Settings

/** A named, isolated key-value store — Android SharedPreferences file / iOS NSUserDefaults suite. */
expect fun createSettings(name: String): Settings
