package com.yourorg.app.Core.Localization

/** Mirrors SettingsStore's persisted language codes ("en"/"es"/"fr"/"de"/"it"). */
enum class AppLanguage(val code: String) {
    EN("en"), ES("es"), FR("fr"), DE("de"), IT("it");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: EN
    }
}
