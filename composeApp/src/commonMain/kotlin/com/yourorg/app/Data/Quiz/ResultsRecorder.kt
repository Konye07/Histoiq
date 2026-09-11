package com.yourorg.app.Data.Quiz

import com.yourorg.app.Core.Haptics.Haptics

data class RankResult(val rank: Int, val totalPlayers: Int, val distribution: List<Double>)

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
    val (rank, totalPlayers) = DailyDistribution.rankAmongPlayers(score, distribution, higherIsBetter)
    return RankResult(rank, totalPlayers, distribution)
}
