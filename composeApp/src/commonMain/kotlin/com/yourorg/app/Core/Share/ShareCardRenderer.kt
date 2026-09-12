package com.yourorg.app.Core.Share

/**
 * Draws a square, purpose-made "share card" (app name, role, score, ranking) rather than
 * screenshotting the live UI — a raw capture would include the status bar/back button/nav bar,
 * and different games' result layouts vary in height, so a dedicated render is both simpler and
 * more consistent to share. Returns PNG-encoded bytes directly (not a platform bitmap type) so the
 * expect/actual boundary doesn't need a shared bitmap representation — each platform draws with
 * its own native 2D API (Android: android.graphics.Canvas; iOS: UIGraphicsImageRenderer) and
 * PNG-encodes internally.
 */
expect object ShareCardRenderer {
    fun renderPng(
        appName: String,
        roleName: String,
        gameTitle: String,
        scoreLabel: String,
        scoreText: String,
        rankingText: String,
        accentColorArgb: Int,
        footerText: String
    ): ByteArray
}
