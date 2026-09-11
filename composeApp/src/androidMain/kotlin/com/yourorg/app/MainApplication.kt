package com.yourorg.app

import android.app.Application
import com.yourorg.app.Core.Platform.AndroidPlatformContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatformContext.app = this
    }
}
