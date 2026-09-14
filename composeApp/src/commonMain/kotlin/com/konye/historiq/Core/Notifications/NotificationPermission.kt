package com.konye.historiq.Core.Notifications

import androidx.compose.runtime.Composable

/**
 * Returns a function that triggers the platform's runtime notification-permission flow (a no-op
 * on platforms/OS versions that don't require one), invoking [onResult] with the outcome. Needs to
 * be `expect`/`actual` rather than a plain `expect fun`: Android's request is a Compose-scoped
 * `rememberLauncherForActivityResult` call, which only exists in `androidx.activity.compose`.
 */
@Composable
expect fun rememberRequestNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit
