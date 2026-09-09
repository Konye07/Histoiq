package com.yourorg.app.Data.Settings

import android.content.Context

/** User-facing app preferences: language, haptics, daily reminder. */
object SettingsStore {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_HAPTICS_ENABLED = "hapticsEnabled"
    private const val KEY_DAILY_REMINDER_ENABLED = "dailyReminderEnabled"

    const val LANGUAGE_ENGLISH = "en"

    fun language(context: Context): String =
        prefs(context).getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH

    fun setLanguage(context: Context, language: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun hapticsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAPTICS_ENABLED, true)

    fun setHapticsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
    }

    fun dailyReminderEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DAILY_REMINDER_ENABLED, false)

    fun setDailyReminderEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
