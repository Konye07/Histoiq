package com.konye.historiq.Core.Platform

import androidx.compose.runtime.Composable

@Composable
actual fun ApplyStatusBarStyle(isDarkTheme: Boolean) {
    // No-op: iOS already draws content under the status bar by default, and Compose
    // Multiplatform surfaces its safe area through the same WindowInsets API with no
    // separate edge-to-edge opt-in needed (unlike Android pre-15).
}
