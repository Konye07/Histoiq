package com.konye.historiq.Core.Network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_info
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private const val SHOW_AFTER_OFFLINE_MS = 1_500L

/** App-wide offline warning, shown only after [SHOW_AFTER_OFFLINE_MS] offline (iOS's monitor reports offline until its first update). */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    val online by NetworkMonitor.isOnline.collectAsState()
    var showWarning by remember { mutableStateOf(false) }
    LaunchedEffect(online) {
        if (online) {
            showWarning = false
        } else {
            delay(SHOW_AFTER_OFFLINE_MS)
            showWarning = true
        }
    }
    AnimatedVisibility(
        visible = showWarning,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Surface(color = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(Res.drawable.ic_info), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    StringsHomeShared.offline_banner_message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}
