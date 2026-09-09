package com.yourorg.app.Features.Play.Shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Top-right "Hint" action for a question screen: tapping it asks to watch an ad, and on
 * confirmation reveals [hint] (there's no real ad SDK yet, so "watching" completes immediately —
 * the same mock-now pattern used elsewhere in this app). Once revealed for the current question,
 * the button no longer opens the dialog — callers should hide it entirely once [hintText] is showing.
 */
@Composable
fun HintAction(onWatchAd: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    TextButton(onClick = { showDialog = true }) {
        Text("Hint")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Want a hint?") },
            text = { Text("Watch a short ad to find out which war or conflict this is part of.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onWatchAd()
                }) { Text("Watch Ad") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}
