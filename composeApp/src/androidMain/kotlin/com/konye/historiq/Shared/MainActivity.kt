package com.konye.historiq.Shared

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.konye.historiq.App
import com.konye.historiq.Core.Auth.handleFacebookOAuthDeeplink
import com.konye.historiq.Core.Auth.notifyFacebookOAuthResumed
import com.konye.historiq.Core.Platform.AndroidPlatformContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AndroidPlatformContext.currentActivity = this
        handleFacebookOAuthDeeplink(intent)
        setContent {
            App()
        }
    }

    // "Continue with Facebook" (Core/Auth/FacebookSignIn.android.kt) returns via this deep link —
    // android:launchMode="singleTask" (AndroidManifest.xml) routes it here instead of a new instance.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleFacebookOAuthDeeplink(intent)
    }

    override fun onResume() {
        super.onResume()
        notifyFacebookOAuthResumed()
    }

    override fun onDestroy() {
        if (AndroidPlatformContext.currentActivity === this) AndroidPlatformContext.currentActivity = null
        super.onDestroy()
    }
}
