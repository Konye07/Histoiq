package com.yourorg.app.Features.Play.Shared

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourorg.app.Core.Localization.StringsHomeShared
import com.yourorg.app.generated.resources.Res
import com.yourorg.app.generated.resources.ic_lightbulb
import org.jetbrains.compose.resources.painterResource

/**
 * Top-right "Hint" action for a question screen: tapping it asks to watch an ad, and on
 * confirmation reveals [hint] (there's no real ad SDK yet, so "watching" completes immediately —
 * the same mock-now pattern used elsewhere in this app). Once revealed for the current question,
 * the button no longer opens the dialog — callers should hide it entirely once [hintText] is showing.
 */
@Composable
fun HintAction(onWatchAd: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(painterResource(Res.drawable.ic_lightbulb), contentDescription = null, modifier = Modifier.size(18.dp))
        Text(StringsHomeShared.hint_label, modifier = Modifier.padding(start = 6.dp))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(StringsHomeShared.hint_dialog_title) },
            text = { Text(StringsHomeShared.hint_dialog_text) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onWatchAd()
                }) { Text(StringsHomeShared.hint_watch_ad, color = MaterialTheme.colorScheme.onSurface) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(StringsHomeShared.hint_cancel, color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    }
}
