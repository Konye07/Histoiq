package com.konye.historiq.Data.Quiz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.konye.historiq.Core.Haptics.Haptics

data class RankResult(val rank: Int, val totalPlayers: Int, val distribution: List<Double>, val topPercent: Int)

/**
 * The "finish a round" sequence every game's results screen runs: record the score, update
 * lifetime stats, buzz on a perfect run, and compute today's rank against the real distribution —
 * consolidates what used to be six near-identical LaunchedEffect(Unit) blocks (see the maintainability
 * audit). [distribution] is included on the result so callers that also render the raw score
 * spread (e.g. Herald's final-multiplier chart) don't have to fetch it a second time.
 */
suspend fun recordAndRank(
    progressKey: String,
    gameKey: String,
    score: Double,
    isPerfect: Boolean,
    higherIsBetter: Boolean = true
): RankResult {
    ScoreHistoryStore.recordResult(progressKey, score)
    StatsStore.recordCompletion(progressKey, score, isPerfect)
    if (isPerfect) Haptics.perfect()

    val distribution = DailyDistribution.finalScores(gameKey, DailyClock.todayKey())
    val (rank, totalPlayers, topPercent) = DailyDistribution.rankAmongPlayers(score, distribution, higherIsBetter)
    return RankResult(rank, totalPlayers, distribution, topPercent)
}

/**
 * [recordAndRank], re-run every time this screen actually becomes visible again — not just once at
 * first composition. A plain `LaunchedEffect(Unit)` only fires when this composable is freshly
 * entered; if the app is simply backgrounded and resumed (no process death, no re-navigation, this
 * composition never gets disposed), the rank/total shown would otherwise sit frozen at whatever it
 * was when the player first finished, even as more players finish the same game throughout the day
 * (confirmed live: a player's displayed rank stayed "1 of 3" after a better score was recorded by
 * someone else, when a fresh computation against the real data already gave rank 2).
 */
@Composable
fun rememberLiveRank(
    progressKey: String,
    gameKey: String,
    score: Double,
    isPerfect: Boolean,
    higherIsBetter: Boolean = true
): RankResult {
    var result by remember { mutableStateOf(RankResult(1, 1, emptyList(), 100)) }
    var resumeTrigger by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) resumeTrigger++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(resumeTrigger) {
        result = recordAndRank(progressKey, gameKey, score, isPerfect, higherIsBetter)
    }
    return result
}
