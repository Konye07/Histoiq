package com.konye.historiq.Core.Theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect

/**
 * The app's loading placeholder everywhere content is still on its way — the same torch the
 * streak badge uses, gently flickering, instead of a bare Material spinner. Falls back to a plain
 * [CircularProgressIndicator] for the brief window before the torch bitmap itself has loaded (and
 * forever, if it fails to load at all), so this is never a regression on the old behavior.
 */
@Composable
fun HistoriqLoadingIndicator(modifier: Modifier = Modifier, size: Dp = 56.dp) {
    var torch by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(Unit) {
        torch = withContext(Dispatchers.Default) { tryLoadImageBitmap("images/streak_torch.png") }
    }

    val bitmap = torch
    if (bitmap == null) {
        CircularProgressIndicator(modifier = modifier.size(size))
    } else {
        val infinite = rememberInfiniteTransition(label = "torchFlicker")
        val flicker by infinite.animateFloat(
            initialValue = 0.7f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "flickerAlpha"
        )
        val wobble by infinite.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "flickerRotation"
        )
        val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .size(size, size / aspect)
                .graphicsLayer {
                    alpha = flicker
                    rotationZ = wobble
                    val s = 0.92f + 0.08f * flicker
                    scaleX = s
                    scaleY = s
                }
        )
    }
}
