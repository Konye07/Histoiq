package com.yourorg.app.Core.Share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

private const val TAG = "ShareResults"

/**
 * Renders a share card for one game's result and opens the system share sheet with it, alongside
 * text carrying the score and a link to the app's Play Store listing. The image is written to
 * cacheDir/share_images (declared in res/xml/file_paths.xml) and shared via a FileProvider content
 * Uri — a raw file:// Uri would be rejected by other apps' targeting API 24+ StrictMode policy.
 */
fun shareResults(
    context: Context,
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

        val imagesDir = File(context.cacheDir, "share_images").apply { mkdirs() }
        val file = File(imagesDir, "result_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, chooserTitle)
        if (context !is android.app.Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to share results", e)
    }
}

/** The app's own Play Store listing — resolves correctly once the app is actually published under this id. */
fun playStoreUrl(context: Context): String =
    "https://play.google.com/store/apps/details?id=${context.packageName}"
