package com.konye.historiq.Features.Profile

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.StringsSocial
import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.GameStats
import com.konye.historiq.Data.Quiz.StatsStore

@Composable
fun ProfileScreen() {
    val account = remember { AccountRepository.currentAccount() }
    var statsByGame by remember { mutableStateOf<Map<String, GameStats>>(emptyMap()) }

    LaunchedEffect(Unit) {
        statsByGame = GameKeys.ALL.associateWith { key -> StatsStore.statsFor(key) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(StringsSocial.profile_title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (account != null) {
            Text(account.displayName, style = MaterialTheme.typography.titleMedium)
            Text(account.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }

        HorizontalDivider()
        Text(StringsSocial.profile_statistics, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        if (account == null) {
            Text(
                StringsSocial.profile_guest_notice,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            GameKeys.ALL.forEach { key ->
                val stats = statsByGame[key] ?: GameStats(0, 0.0, 0)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GameKeys.color(key).copy(alpha = 0.12f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            StringsSocial.profile_role_title_format(GameKeys.roleName(key), GameKeys.title(key)),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (stats.gamesPlayed == 0) {
                            Text(StringsSocial.profile_not_played_yet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        } else {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatColumn(StringsSocial.profile_played, "${stats.gamesPlayed}")
                                StatColumn(StringsSocial.profile_average, GameKeys.formatScore(key, stats.averageScore))
                                StatColumn(StringsSocial.profile_perfect_runs, "${stats.perfectCount}")
                            }
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
