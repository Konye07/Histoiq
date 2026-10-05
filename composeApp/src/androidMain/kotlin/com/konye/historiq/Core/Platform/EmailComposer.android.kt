package com.konye.historiq.Core.Platform

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri

actual fun openEmailComposer(email: String, onUnavailable: () -> Unit) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    // Not resolveActivity() first: on Android 11+ that sees no mail app without a <queries> entry, even when one is installed.
    try {
        AndroidPlatformContext.app.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        onUnavailable()
    }
}
