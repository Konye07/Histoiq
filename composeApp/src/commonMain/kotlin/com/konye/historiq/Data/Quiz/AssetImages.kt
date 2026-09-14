package com.konye.historiq.Data.Quiz

import androidx.compose.ui.graphics.ImageBitmap
import com.konye.historiq.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Never throws — returns null if the path is blank, missing, or fails to decode. [assetPath] comes
 * straight from a database row's `image_asset` column (e.g. "images/flags/flag_japan_1930.png"),
 * so it needs only a "files/" prefix to reach the same file under Compose Multiplatform's shared
 * resource store — the DB never needed to know it moved off Android's asset manager.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun tryLoadImageBitmap(assetPath: String?): ImageBitmap? {
    if (assetPath.isNullOrBlank()) return null
    return try {
        Res.readBytes("files/$assetPath").decodeToImageBitmap()
    } catch (e: Exception) {
        null
    }
}
