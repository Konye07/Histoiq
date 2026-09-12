package com.yourorg.app.Core.Share

/**
 * Real sharing (UIActivityViewController + a temp file — iOS has no ContentProvider equivalent, so
 * this is a genuinely different mechanism, not a port) and the real App Store URL (once the app has
 * one) arrive with the rest of the isolated platform-specific concerns. A safe no-op for now: Share
 * Results isn't reachable from any iOS screen yet either, so nothing depends on this being real yet.
 */
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
}

actual fun storeUrl(): String = ""
