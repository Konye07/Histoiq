package com.konye.historiq.Core.Network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.konye.historiq.Core.Platform.AndroidPlatformContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual object NetworkMonitor {
    private val connectivityManager by lazy {
        AndroidPlatformContext.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val _isOnline = MutableStateFlow(false)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        _isOnline.value = currentlyOnline()
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = currentlyOnline()
            }

            // activeNetwork can still return the network being lost while onLost runs, so ignore it.
            override fun onLost(network: Network) {
                _isOnline.value = currentlyOnline(excluding = network)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                _isOnline.value = currentlyOnline()
            }
        })
    }

    private fun currentlyOnline(excluding: Network? = null): Boolean {
        val network = connectivityManager.activeNetwork?.takeIf { it != excluding } ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.isUsable()
    }

    private fun NetworkCapabilities.isUsable(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
