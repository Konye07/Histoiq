package com.yourorg.app.Core.Notifications

/**
 * Schedules/cancels the once-a-day "come play today's quizzes" reminder. Android uses
 * AlarmManager+BroadcastReceiver (fires fresh once a day); iOS uses a single repeating
 * `UNCalendarNotificationTrigger` with its content baked in at schedule-time — see
 * [ReminderText] for the shared, localized notification copy both platforms show.
 */
expect object ReminderScheduler {
    fun schedule()
    fun cancel()
}
