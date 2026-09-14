package com.konye.historiq.Core.Network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_info
import org.jetbrains.compose.resources.painterResource

/**
 * Wraps a game screen so it only renders while online — every game fetches its daily question
 * set from Supabase regardless of guest/signed-in status (see NetworkMonitor's own doc comment),
 * so there's nothing useful to show offline. Reactive: reverts to [content] the instant
 * [NetworkMonitor.isOnline] flips back to true, no manual retry needed.
 */
@Composable
fun GameGate(content: @Composable () -> Unit) {
    val online by NetworkMonitor.isOnline.collectAsState()
    if (online) content() else NoConnectionScreen()
}

@Composable
private fun NoConnectionScreen() {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painterResource(Res.drawable.ic_info),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.size(16.dp))
        Text(
            StringsHomeShared.no_connection_title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(8.dp))
        Text(
            StringsHomeShared.no_connection_body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}
