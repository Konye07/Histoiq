package com.konye.historiq.Core.Network

import kotlinx.coroutines.flow.StateFlow

/** Every game needs the network (the daily quiz set is fetched from Supabase even for guests); [OfflineBanner] reads [isOnline] to warn the player. */
expect object NetworkMonitor {
    val isOnline: StateFlow<Boolean>
}
