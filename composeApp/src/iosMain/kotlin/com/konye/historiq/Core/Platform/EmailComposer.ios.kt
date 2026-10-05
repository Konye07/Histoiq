package com.konye.historiq.Core.Platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openEmailComposer(email: String, onUnavailable: () -> Unit) {
    val url = NSURL.URLWithString("mailto:$email")
    if (url == null) {
        onUnavailable()
        return
    }
    // The one-argument openURL() is deprecated and reports nothing useful; this one says whether anything opened.
    UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = { opened ->
        if (!opened) onUnavailable()
    })
}
