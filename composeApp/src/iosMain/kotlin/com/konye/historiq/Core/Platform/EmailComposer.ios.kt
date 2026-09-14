package com.konye.historiq.Core.Platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openEmailComposer(email: String) {
    NSURL.URLWithString("mailto:$email")?.let { UIApplication.sharedApplication.openURL(it) }
}
