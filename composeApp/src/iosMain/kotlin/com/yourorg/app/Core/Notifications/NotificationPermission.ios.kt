package com.yourorg.app.Core.Notifications

import androidx.compose.runtime.Composable

/**
 * Real authorization (`UNUserNotificationCenter.requestAuthorization`) arrives with the rest of
 * the isolated platform-specific concerns. A safe stub for now — always reports granted, since the
 * daily-reminder toggle isn't reachable from any iOS screen yet either.
 */
@Composable
actual fun rememberRequestNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit {
    return { onResult(true) }
}
