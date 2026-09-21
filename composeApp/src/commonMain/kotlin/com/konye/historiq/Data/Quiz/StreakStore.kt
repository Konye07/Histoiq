package com.konye.historiq.Data.Quiz

import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [days] is the current consecutive-day count. [playedToday] false with days > 0 means the streak
 * is still alive but at risk — yesterday's chain is intact, today's link just isn't played yet.
 */
data class GameStreak(val days: Int, val playedToday: Boolean)

/**
 * Streaks are derived from score_history's existing per-day rows rather than a separate counter
 * that could drift out of sync with it — one row per (user, game, day) already means "completed
 * that day", for a guest's anonymous session exactly as much as a signed-in account's (see
 * [ScoreHistoryStore]).
 */
object StreakStore {
    suspend fun computeAll(): Map<String, GameStreak> {
        val userId = AccountRepository.ensureAnonymousSession()
        val rows = SupabaseClient.client.postgrest.from("score_history")
            .select(Columns.list("game_key", "date_key")) { filter { eq("user_id", userId) } }
            .decodeList<GameDateRow>()
        val datesByGame = rows.groupBy({ it.gameKey }, { it.dateKey }).mapValues { it.value.toHashSet() }
        return GameKeys.ALL.associateWith { key -> streakFor(datesByGame[key] ?: emptySet()) }
    }

    /** True when at least one of the six games has no score_history row for today yet. */
    suspend fun hasUnfinishedGameToday(): Boolean {
        val userId = AccountRepository.ensureAnonymousSession()
        val playedToday = SupabaseClient.client.postgrest.from("score_history")
            .select(Columns.list("game_key")) {
                filter {
                    eq("user_id", userId)
                    eq("date_key", DailyClock.todayKey())
                }
            }
            .decodeList<GameKeyRow>()
            .mapTo(HashSet()) { it.gameKey }
        return GameKeys.ALL.any { it !in playedToday }
    }

    private fun streakFor(playedDates: Set<String>): GameStreak {
        val playedToday = DailyClock.dateKey(0) in playedDates
        if (!playedToday && DailyClock.dateKey(1) !in playedDates) return GameStreak(0, false)
        var count = 0
        var offset = if (playedToday) 0 else 1
        while (DailyClock.dateKey(offset) in playedDates) {
            count++
            offset++
        }
        return GameStreak(count, playedToday)
    }
}

@Serializable
private data class GameDateRow(
    @SerialName("game_key") val gameKey: String,
    @SerialName("date_key") val dateKey: String
)

@Serializable
private data class GameKeyRow(@SerialName("game_key") val gameKey: String)
