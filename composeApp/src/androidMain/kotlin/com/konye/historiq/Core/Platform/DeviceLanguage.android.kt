package com.konye.historiq.Core.Platform

import android.os.LocaleList

actual fun devicePreferredLanguages(): List<String> {
    val locales = LocaleList.getDefault()
    return (0 until locales.size()).map { locales[it].language }
}
