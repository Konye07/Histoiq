package com.konye.historiq.Shared

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.konye.historiq.App
import com.konye.historiq.Core.Platform.AndroidPlatformContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidPlatformContext.currentActivity = this
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        if (AndroidPlatformContext.currentActivity === this) AndroidPlatformContext.currentActivity = null
        super.onDestroy()
    }
}
