package com.konye.historiq.Core.Localization

import com.konye.historiq.Core.Platform.devicePreferredLanguages

/** Mirrors SettingsStore's persisted language codes ("en"/"es"/"fr"/"de"/"it"/"hu"); [nativeName] is the language written in itself. */
enum class AppLanguage(val code: String, val nativeName: String) {
    EN("en", "English"),
    ES("es", "Español"),
    FR("fr", "Français"),
    DE("de", "Deutsch"),
    IT("it", "Italiano"),
    HU("hu", "Magyar");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: EN

        /** The first of the device's preferred languages the app is translated into, English when it has none of them. */
        fun fromDevice(): AppLanguage =
            devicePreferredLanguages().firstNotNullOfOrNull { code -> entries.find { it.code == code } } ?: EN
    }
}
