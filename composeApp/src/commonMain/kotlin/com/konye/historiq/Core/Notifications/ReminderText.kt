package com.konye.historiq.Core.Notifications

import com.konye.historiq.Core.Localization.AppLanguage
import com.konye.historiq.Data.Settings.SettingsStore

/**
 * Notification title/body for the daily reminder. Plain functions rather than the app's usual
 * `@Composable`/`LocalAppLanguage` string tables — a BroadcastReceiver (Android) and a
 * schedule-time call (iOS) both need this outside any composition — so it reads the persisted
 * language straight from [SettingsStore] instead. One fixed line per language rather than a
 * rotating pool: iOS bakes the body into the notification once at schedule-time (a single
 * repeating `UNCalendarNotificationTrigger`), so it can't vary day-to-day the way Android's
 * once-daily `BroadcastReceiver` could — keeping both platforms on one line avoids Android
 * rotating while iOS stays static.
 */
object ReminderText {
    const val title = "Historiq"

    fun body(): String = when (AppLanguage.fromCode(SettingsStore.language())) {
        AppLanguage.EN -> "History is calling. Will you answer today's mysteries?"
        AppLanguage.ES -> "La historia te llama. ¿Responderás a los misterios de hoy?"
        AppLanguage.FR -> "L'histoire t'appelle. Répondras-tu aux mystères du jour ?"
        AppLanguage.DE -> "Die Geschichte ruft. Löst du die Rätsel von heute?"
        AppLanguage.IT -> "La storia ti chiama. Risolverai i misteri di oggi?"
    }
}
