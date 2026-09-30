package com.konye.historiq.Core.Theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.konye.historiq.Data.Quiz.tryLoadImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The app's own historian mascot (the launcher-icon character, cut out to a transparent
 * background) paired with a short message — used wherever a screen would otherwise show nothing
 * but a plain line of muted text (a signed-out Profile, a Friends list with no friends yet).
 */
@Composable
fun MascotEmptyState(message: String, modifier: Modifier = Modifier, mascotSize: Dp = 96.dp) {
    var mascot by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(Unit) {
        mascot = withContext(Dispatchers.Default) { tryLoadImageBitmap("images/mascot.png") }
    }

    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        mascot?.let {
            Image(bitmap = it, contentDescription = null, modifier = Modifier.size(mascotSize))
        }
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}
