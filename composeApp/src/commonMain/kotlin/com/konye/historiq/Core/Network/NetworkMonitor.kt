package com.konye.historiq.Core.Network

import kotlinx.coroutines.flow.StateFlow

/**
 * Every game requires network — daily quiz assignment is fetched from Supabase for guests and
 * signed-in accounts alike (only score/progress storage branches on account status), so there's no
 * local-only question bank to fall back to. Screens gate on [isOnline] rather than only reacting to
 * a failed fetch, so the "you're offline" state shows before a network call is even attempted.
 */
expect object NetworkMonitor {
    val isOnline: StateFlow<Boolean>
}
