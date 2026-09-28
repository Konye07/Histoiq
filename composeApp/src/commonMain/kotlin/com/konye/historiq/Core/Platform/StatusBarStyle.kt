package com.konye.historiq.Core.Platform

import androidx.compose.runtime.Composable

/**
 * Keeps the platform's system-bar icon contrast matching the app's *effective* dark/light theme
 * (LocalIsDarkTheme) rather than the OS's own setting — the two can disagree, since dark mode
 * here is an in-app override, not just a system-follow. iOS needs no equivalent: its status bar
 * already draws over app content by default and Compose Multiplatform exposes its safe area via
 * the same WindowInsets API with no separate opt-in.
 */
@Composable
expect fun ApplyStatusBarStyle(isDarkTheme: Boolean)
