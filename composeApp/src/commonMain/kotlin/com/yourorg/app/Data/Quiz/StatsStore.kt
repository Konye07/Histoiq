package com.yourorg.app.Data.Quiz

import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class GameStats(val gamesPlayed: Int, val averageScore: Double, val perfectCount: Int)

/**
 * Lifetime per-quiz totals live in the shared `game_stats` table, one row per real account — a
 * guest (no signed-in account) never gets a row here at all, on either device or server: a guest's
 * daily result still counts for that one day via [ScoreHistoryStore]/[QuizProgressStore], but
 * nothing lifetime-aggregated is collected until they actually sign in, by design.
 */
object StatsStore {
    /** Idempotent per day: a quiz that's already been recorded for today won't be double-counted. No-op for a guest. */
    suspend fun recordCompletion(quizKey: String, score: Double, isPerfect: Boolean) {
        val account = AccountRepository.currentAccount() ?: return
        val today = DailyClock.todayKey()

        val existingRemote = fetchRemote(account.id, quizKey)
        if (existingRemote?.lastRecordedDate == today) return
        SupabaseClient.client.postgrest.from("game_stats").upsert(
            GameStatsRow(
                userId = account.id,
                gameKey = quizKey,
                gamesPlayed = (existingRemote?.gamesPlayed ?: 0) + 1,
                totalScore = (existingRemote?.totalScore ?: 0.0) + score,
                perfectCount = (existingRemote?.perfectCount ?: 0) + if (isPerfect) 1 else 0,
                lastRecordedDate = today
            )
        )
    }

    /** Empty stats for a guest — there is nothing to fetch, since nothing was ever recorded for one. */
    suspend fun statsFor(quizKey: String): GameStats {
        val account = AccountRepository.currentAccount() ?: return GameStats(0, 0.0, 0)
        val row = fetchRemote(account.id, quizKey) ?: return GameStats(0, 0.0, 0)
        val average = if (row.gamesPlayed > 0) row.totalScore / row.gamesPlayed else 0.0
        return GameStats(row.gamesPlayed, average, row.perfectCount)
    }

    private suspend fun fetchRemote(userId: String, quizKey: String): GameStatsRow? =
        SupabaseClient.client.postgrest.from("game_stats")
            .select { filter { eq("user_id", userId); eq("game_key", quizKey) } }
            .decodeSingleOrNull<GameStatsRow>()
}

@Serializable
private data class GameStatsRow(
    @SerialName("user_id") val userId: String,
    @SerialName("game_key") val gameKey: String,
    @SerialName("games_played") val gamesPlayed: Int,
    @SerialName("total_score") val totalScore: Double,
    @SerialName("perfect_count") val perfectCount: Int,
    @SerialName("last_recorded_date") val lastRecordedDate: String?
)
