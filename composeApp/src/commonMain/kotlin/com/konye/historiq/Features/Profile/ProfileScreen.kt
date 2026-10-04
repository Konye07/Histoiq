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
import com.konye.historiq.Core.Network.networkOrElse
import com.konye.historiq.Core.Theme.MascotEmptyState
import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Quiz.ActiveEra
import com.konye.historiq.Data.Quiz.GameKeys
import com.konye.historiq.Data.Quiz.GameStats
import com.konye.historiq.Data.Quiz.ScoreHistoryStore
import com.konye.historiq.Data.Quiz.StatsStore
import com.konye.historiq.Features.Play.Shared.EraFilterChip

/** Games with a personal-best display in Profile — the point-scale games only, not the two sudden-death survival games. */
private val BEST_SCORE_GAMES = listOf(GameKeys.YEAR_GUESS, GameKeys.CASUALTIES_MULTIPLIER, GameKeys.FLAG_COLOR_MATCH, GameKeys.BLIND_MAP_BATTLE)

@Composable
fun ProfileScreen() {
    val account = remember { AccountRepository.currentAccount() }
    var era by remember { mutableStateOf(ActiveEra.current()) }
    // An era game mode has only Annalist, Herald and Chancellor, so picking an era drops the other games from the list.
    val games = if (era == null) GameKeys.ALL else GameKeys.ALL.filter { it in GameKeys.ERA_FILTERABLE }
    var statsByMode by remember { mutableStateOf<Map<String, GameStats>>(emptyMap()) }
    var bestByMode by remember { mutableStateOf<Map<String, Double?>>(emptyMap()) }

    LaunchedEffect(era) {
        val newStats = mutableMapOf<String, GameStats>()
        games.map { GameKeys.modeKey(it, era) }.filter { it !in statsByMode }.forEach { modeKey ->
            newStats[modeKey] = networkOrElse(GameStats(0, 0.0, 0)) { StatsStore.statsFor(modeKey) }
        }
        statsByMode = statsByMode + newStats

        if (account != null) {
            val newBest = mutableMapOf<String, Double?>()
            BEST_SCORE_GAMES.filter { it in games }.map { it to GameKeys.modeKey(it, era) }
                .filter { (_, modeKey) -> modeKey !in bestByMode }
                .forEach { (key, modeKey) ->
                    newBest[modeKey] = networkOrElse<Double?>(null) {
                        ScoreHistoryStore.bestFor(modeKey, lowerIsBetter = key == GameKeys.CASUALTIES_MULTIPLIER)
                    }
                }
            bestByMode = bestByMode + newBest
        }
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(StringsSocial.profile_statistics, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (account != null) EraFilterChip(era, onEraChange = { era = it })
        }

        if (account == null) {
            MascotEmptyState(StringsSocial.profile_guest_notice, modifier = Modifier.padding(vertical = 16.dp))
        } else {
            games.forEach { key ->
                val modeKey = GameKeys.modeKey(key, era)
                val stats = statsByMode[modeKey]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GameKeys.color(modeKey).copy(alpha = 0.12f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            StringsSocial.profile_role_title_format(GameKeys.roleName(key), GameKeys.title(modeKey)),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (stats != null && stats.gamesPlayed == 0) {
                            Text(StringsSocial.profile_not_played_yet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        } else if (stats != null) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatColumn(StringsSocial.profile_played, "${stats.gamesPlayed}")
                                StatColumn(StringsSocial.profile_average, GameKeys.formatScore(key, stats.averageScore))
                                StatColumn(StringsSocial.profile_perfect_runs, "${stats.perfectCount}")
                                val best = bestByMode[modeKey]
                                if (key in BEST_SCORE_GAMES && best != null) {
                                    StatColumn(StringsSocial.profile_best_score, GameKeys.formatScore(key, best))
                                }
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
