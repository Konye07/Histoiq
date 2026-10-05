package com.konye.historiq.Core.Platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

actual fun copyToClipboard(text: String) {
    val clipboard = AndroidPlatformContext.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Historiq", text))
}
