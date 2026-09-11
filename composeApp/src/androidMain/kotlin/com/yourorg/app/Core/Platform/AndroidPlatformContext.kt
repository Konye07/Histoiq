package com.yourorg.app.Core.Platform

import android.app.Application

/**
 * Populated once in [com.yourorg.app.App.onCreate] so Android `actual` implementations (settings
 * storage, haptics, ...) can reach a Context without threading one through every commonMain call
 * site — iOS needs no equivalent, since NSUserDefaults/UIFeedbackGenerator/etc. don't need an
 * app-level handle passed in.
 */
internal object AndroidPlatformContext {
    lateinit var app: Application
}
