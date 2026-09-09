package com.yourorg.app.Data.Quiz

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** Never throws — returns null if the path is blank, missing, or fails to decode. */
fun tryLoadImageBitmap(context: Context, assetPath: String?): ImageBitmap? {
    if (assetPath.isNullOrBlank()) return null
    return try {
        context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
