package com.yourorg.app.Core.Share

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import com.yourorg.app.Core.Platform.AndroidPlatformContext
import java.io.ByteArrayOutputStream

private const val TAG = "ShareResults"

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
    try {
        val bitmap = ShareCardRenderer.render(
            appName = appName,
            roleName = roleName,
            gameTitle = gameTitle,
            scoreLabel = scoreLabel,
            scoreText = scoreText,
            rankingText = rankingText,
            accentColorArgb = accentColorArgb,
            footerText = footerText
        )

        val pngBytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }
        bitmap.recycle()

        val context = AndroidPlatformContext.app
        val uri = InMemoryShareImageProvider.put(context, pngBytes)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to share results", e)
    }
}

/** Resolves correctly once the app is actually published under this id. */
actual fun storeUrl(): String =
    "https://play.google.com/store/apps/details?id=${AndroidPlatformContext.app.packageName}"
