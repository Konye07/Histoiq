package com.yourorg.app.Core.Share

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
actual fun shareResults(
    appName: String,
    roleName: String,
    gameTitle: String,
    scoreLabel: String,
    scoreText: String,
    rankingText: String,
    accentColorArgb: Int,
    shareMessage: String,
    chooserTitle: String,
    footerText: String
) {
    val pngBytes = ShareCardRenderer.renderPng(
        appName = appName,
        roleName = roleName,
        gameTitle = gameTitle,
        scoreLabel = scoreLabel,
        scoreText = scoreText,
        rankingText = rankingText,
        accentColorArgb = accentColorArgb,
        footerText = footerText
    )
    val imageData = pngBytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = pngBytes.size.toULong())
    }
    val image = UIImage(data = imageData)

    val activityController = UIActivityViewController(
        activityItems = listOfNotNull(shareMessage, image),
        applicationActivities = null
    )
    topViewController()?.presentViewController(activityController, animated = true, completion = null)
}

/** Resolves correctly once the app has a real App Store listing. */
actual fun storeUrl(): String = ""

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}
