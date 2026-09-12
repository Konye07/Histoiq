package com.yourorg.app.Core.Platform

import android.content.Intent
import android.net.Uri

actual fun openUrl(url: String) {
    val context = AndroidPlatformContext.app
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
