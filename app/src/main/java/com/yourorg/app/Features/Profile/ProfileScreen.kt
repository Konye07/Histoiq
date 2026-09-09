package com.yourorg.app.Features.Profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Quiz.GameKeys
import com.yourorg.app.Data.Quiz.StatsStore

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val account = remember { AccountRepository.currentAccount(context) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (account != null) {
            Text(account.displayName, style = MaterialTheme.typography.titleMedium)
            Text(account.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }

        HorizontalDivider()
        Text("Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        GameKeys.ALL.forEach { key ->
            val stats = remember(key) { StatsStore.statsFor(context, key) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GameKeys.color(key).copy(alpha = 0.12f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "${GameKeys.roleName(key)} — ${GameKeys.title(key)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (stats.gamesPlayed == 0) {
                        Text("Not played yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatColumn("Played", "${stats.gamesPlayed}")
                            StatColumn("Average", GameKeys.formatScore(key, stats.averageScore))
                            StatColumn("Perfect Runs", "${stats.perfectCount}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
