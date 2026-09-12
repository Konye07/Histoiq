package com.yourorg.app.Core.Notifications

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

private const val REQUEST_ID = "daily_reminder"
private const val REMINDER_HOUR = 18L

@OptIn(ExperimentalForeignApi::class)
actual object ReminderScheduler {
    actual fun schedule() {
        val content = UNMutableNotificationContent().apply {
            setTitle(ReminderText.title)
            setBody(ReminderText.body())
        }
        val dateComponents = NSDateComponents().apply {
            hour = REMINDER_HOUR
            minute = 0L
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents, repeats = true)
        val request = UNNotificationRequest.requestWithIdentifier(REQUEST_ID, content, trigger)
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request, null)
    }

    actual fun cancel() {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(REQUEST_ID))
    }
}
