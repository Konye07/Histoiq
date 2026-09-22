package com.konye.historiq.Core.Theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap

/**
 * A faint per-game watermark drawn behind a screen's content. Each game's [gameColorScheme] uses
 * its own base color as the actual background/surface (not a fixed white/cream), so the source
 * line art — transparent-background PNGs under "images/backgrounds/" — is recolored to [tint]
 * (pass `MaterialTheme.colorScheme.onBackground`, already contrast-correct for that base color
 * via [contrastingOn]) rather than left as its native black ink, or it would vanish on a dark base.
 *
 * [ContentScale.Fit] against the screen's own full box (no padding) is deliberate, not just the
 * simplest option: it's the one scale mode that's mathematically guaranteed to show the whole
 * bitmap at the largest size that still fits entirely on screen — anything bigger (e.g. scaling
 * past the box) starts clipping the art against the physical screen edge instead of framing it.
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
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        alpha = 0.13f,
        colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn)
    )
}
