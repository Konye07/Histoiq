package com.yourorg.app.Data.Quiz

import com.yourorg.app.Core.Platform.createSettings
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class GameStats(val gamesPlayed: Int, val averageScore: Double, val perfectCount: Int)

/**
 * Lifetime per-quiz totals live in the shared `game_stats` table for a signed-in account (so
 * Profile stats follow the account, not the device), or device-local settings storage for guests.
 */
object StatsStore {
    private val settings = createSettings("game_stats")

    /** Idempotent per day: a quiz that's already been recorded for today won't be double-counted. */
    suspend fun recordCompletion(quizKey: String, score: Double, isPerfect: Boolean) {
        val account = AccountRepository.currentAccount()
        val today = DailyClock.todayKey()
        if (account == null) {
            val existing = loadRaw(quizKey)
            if (existing.lastRecordedDate == today) return
            val updated = existing.copy(
                gamesPlayed = existing.gamesPlayed + 1,
                totalScore = existing.totalScore + score,
                perfectCount = existing.perfectCount + if (isPerfect) 1 else 0,
                lastRecordedDate = today
            )
            settings.putString(quizKey, Json.encodeToString(updated))
            return
        }

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

    suspend fun statsFor(quizKey: String): GameStats {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            val raw = loadRaw(quizKey)
            val average = if (raw.gamesPlayed > 0) raw.totalScore / raw.gamesPlayed else 0.0
            return GameStats(raw.gamesPlayed, average, raw.perfectCount)
        }

        val row = fetchRemote(account.id, quizKey) ?: return GameStats(0, 0.0, 0)
        val average = if (row.gamesPlayed > 0) row.totalScore / row.gamesPlayed else 0.0
        return GameStats(row.gamesPlayed, average, row.perfectCount)
    }

    fun clearAll() {
        settings.clear()
    }

    private suspend fun fetchRemote(userId: String, quizKey: String): GameStatsRow? =
        SupabaseClient.client.postgrest.from("game_stats")
            .select { filter { eq("user_id", userId); eq("game_key", quizKey) } }
            .decodeSingleOrNull<GameStatsRow>()

    /** Old org.json-era data that fails to parse under the new format is treated as "no stats yet," not a crash. */
    private fun loadRaw(quizKey: String): GuestStats {
        val raw = settings.getStringOrNull(quizKey) ?: return GuestStats()
        return runCatching { Json.decodeFromString<GuestStats>(raw) }.getOrDefault(GuestStats())
    }
}

@Serializable
private data class GuestStats(
    val gamesPlayed: Int = 0,
    val totalScore: Double = 0.0,
    val perfectCount: Int = 0,
    val lastRecordedDate: String? = null
)

@Serializable
private data class GameStatsRow(
    @SerialName("user_id") val userId: String,
    @SerialName("game_key") val gameKey: String,
    @SerialName("games_played") val gamesPlayed: Int,
    @SerialName("total_score") val totalScore: Double,
    @SerialName("perfect_count") val perfectCount: Int,
    @SerialName("last_recorded_date") val lastRecordedDate: String?
)
