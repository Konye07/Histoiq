package com.yourorg.app.Core.Network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.yourorg.app.Core.Platform.AndroidPlatformContext
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
                _isOnline.value = true
            }

            override fun onLost(network: Network) {
                _isOnline.value = currentlyOnline()
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                _isOnline.value = capabilities.isUsable()
            }
        })
    }

    private fun currentlyOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.isUsable()
    }

    private fun NetworkCapabilities.isUsable(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
