package com.yourorg.app.Core.Notifications

/**
 * Schedules/cancels the once-a-day "come play today's quizzes" reminder. The iOS actual is a
 * no-op stub for now (deferred to a later phase's real `UNUserNotificationCenter` implementation,
 * which uses a different scheduling model entirely from Android's AlarmManager+BroadcastReceiver).
 */
expect object ReminderScheduler {
    fun schedule()
    fun cancel()
}
