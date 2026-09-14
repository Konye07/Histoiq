package com.konye.historiq.Core.Share

/**
 * Renders a share card for one game's result and opens the platform share sheet with it, alongside
 * text carrying the score and a link to the app's store listing. The image is never written to
 * disk — PNG-encoded straight into memory and handed to the OS share mechanism, forgotten right
 * after (see the Android actual's InMemoryShareImageProvider for exactly how).
 */
expect fun shareResults(
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
)

/** The app's own store listing — Play Store on Android, App Store on iOS. */
expect fun storeUrl(): String
