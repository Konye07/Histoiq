package com.konye.historiq

import android.app.Application
import com.konye.historiq.Core.Platform.AndroidPlatformContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatformContext.app = this
    }
}
