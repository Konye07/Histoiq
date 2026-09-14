package com.konye.historiq.Core.Platform

import android.content.Intent
import android.net.Uri

actual fun openEmailComposer(email: String) {
    val context = AndroidPlatformContext.app
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
