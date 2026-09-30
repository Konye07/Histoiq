package com.konye.historiq.Core.Theme

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The one key shared between the splash-screen logo and PlayScreen's docked logo, so [androidx.compose.animation.SharedTransitionScope] recognizes them as the same element crossing a navigation boundary. */
const val APP_LOGO_SHARED_KEY = "app_logo"

/**
 * The app's own main logo (the historian character on its gold badge) — shown full-size on the
 * splash/loading screen and, at a fraction of the size, docked in the Play screen's header. Two
 * independent loads (one per call site) rather than a shared cache: it's a small local asset, and
 * this keeps each call site simple and self-contained like every other image load in this app.
 */
@Composable
fun AppLogoImage(modifier: Modifier = Modifier) {
    var logo by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(Unit) {
        logo = withContext(Dispatchers.Default) { tryLoadImageBitmap("images/main_logo.png") }
    }
    logo?.let {
        Image(bitmap = it, contentDescription = null, contentScale = ContentScale.Fit, modifier = modifier)
    }
}
