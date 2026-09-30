package com.konye.historiq.Core.Theme

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * Lets the splash-screen logo (RootApp's "restoring" state) and the docked logo (PlayScreen's
 * header) share one [SharedTransitionScope] despite living in completely different branches of
 * the navigation tree — provided once, high up, by whichever composable wraps its content in
 * `SharedTransitionLayout` (see RootApp.kt). Null in any context that hasn't set one up (e.g. a
 * screen previewed in isolation), so callers must null-check before using it.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
