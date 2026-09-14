package com.yourorg.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.yourorg.app.Core.Platform.AndroidPlatformContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatformContext.app = this
        MobileAds.initialize(this)
    }
}
