package com.konye.historiq.Core.Platform

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun devicePreferredLanguages(): List<String> =
    NSLocale.preferredLanguages.mapNotNull { (it as? String)?.substringBefore('-')?.substringBefore('_')?.lowercase() }
