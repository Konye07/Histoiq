package com.konye.historiq.Core.Theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap

/**
 * A faint per-game watermark drawn behind a screen's content. Each game's [gameColorScheme] uses
 * its own base color as the actual background/surface (not a fixed white/cream), so the source
 * line art — transparent-background PNGs under "images/backgrounds/" — is recolored to [tint]
 * (pass `MaterialTheme.colorScheme.onBackground`, already contrast-correct for that base color
 * via [contrastingOn]) rather than left as its native black ink, or it would vanish on a dark base.
 */
@Composable
fun GameBackgroundArt(assetPath: String, tint: Color, modifier: Modifier = Modifier) {
    val bitmap by produceState<ImageBitmap?>(null, assetPath) {
        value = tryLoadImageBitmap(assetPath)
    }
    val loaded = bitmap ?: return
    Image(
        bitmap = loaded,
        contentDescription = null,
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentScale = ContentScale.Fit,
        alpha = 0.06f,
        colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn)
    )
}
