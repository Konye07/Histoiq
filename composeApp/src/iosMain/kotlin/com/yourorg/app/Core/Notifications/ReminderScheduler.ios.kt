package com.yourorg.app.Core.Notifications

/**
 * Real scheduling (`UNUserNotificationCenter` + a calendar/time-interval trigger — a genuinely
 * different model from Android's AlarmManager+BroadcastReceiver, not a port) arrives with the rest
 * of the isolated platform-specific concerns. A safe no-op for now: the daily-reminder toggle isn't
 * reachable from any iOS screen yet either, so nothing depends on this being real yet.
 */
actual object ReminderScheduler {
    actual fun schedule() {}
    actual fun cancel() {}
}
