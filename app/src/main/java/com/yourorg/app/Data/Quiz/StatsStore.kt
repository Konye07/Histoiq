package com.yourorg.app.Data.Quiz

import android.content.Context
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject

data class GameStats(val gamesPlayed: Int, val averageScore: Double, val perfectCount: Int)

/**
 * Lifetime per-quiz totals live in the shared `game_stats` table for a signed-in account (so
 * Profile stats follow the account, not the device), or device-local SharedPreferences for guests.
 */
object StatsStore {
    private const val PREFS_NAME = "game_stats"

    /** Idempotent per day: a quiz that's already been recorded for today won't be double-counted. */
    suspend fun recordCompletion(context: Context, quizKey: String, score: Double, isPerfect: Boolean) {
        val account = AccountRepository.currentAccount()
        val today = DailyClock.todayKey()
        if (account == null) {
            val obj = loadRaw(context, quizKey)
            if (obj.optString("lastRecordedDate") == today) return
            obj.put("gamesPlayed", obj.optInt("gamesPlayed", 0) + 1)
            obj.put("totalScore", obj.optDouble("totalScore", 0.0) + score)
            obj.put("perfectCount", obj.optInt("perfectCount", 0) + if (isPerfect) 1 else 0)
            obj.put("lastRecordedDate", today)
            prefs(context).edit().putString(quizKey, obj.toString()).apply()
            return
        }

        val existing = fetchRemote(account.id, quizKey)
        if (existing?.lastRecordedDate == today) return
        SupabaseClient.client.postgrest.from("game_stats").upsert(
            GameStatsRow(
                userId = account.id,
                gameKey = quizKey,
                gamesPlayed = (existing?.gamesPlayed ?: 0) + 1,
                totalScore = (existing?.totalScore ?: 0.0) + score,
                perfectCount = (existing?.perfectCount ?: 0) + if (isPerfect) 1 else 0,
                lastRecordedDate = today
            )
        )
    }

    suspend fun statsFor(context: Context, quizKey: String): GameStats {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            val obj = loadRaw(context, quizKey)
            val gamesPlayed = obj.optInt("gamesPlayed", 0)
            val totalScore = obj.optDouble("totalScore", 0.0)
            val perfectCount = obj.optInt("perfectCount", 0)
            val average = if (gamesPlayed > 0) totalScore / gamesPlayed else 0.0
            return GameStats(gamesPlayed, average, perfectCount)
        }

        val row = fetchRemote(account.id, quizKey) ?: return GameStats(0, 0.0, 0)
        val average = if (row.gamesPlayed > 0) row.totalScore / row.gamesPlayed else 0.0
        return GameStats(row.gamesPlayed, average, row.perfectCount)
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private suspend fun fetchRemote(userId: String, quizKey: String): GameStatsRow? =
        SupabaseClient.client.postgrest.from("game_stats")
            .select { filter { eq("user_id", userId); eq("game_key", quizKey) } }
            .decodeSingleOrNull<GameStatsRow>()

    private fun loadRaw(context: Context, quizKey: String): JSONObject {
        val raw = prefs(context).getString(quizKey, null) ?: return JSONObject()
        return JSONObject(raw)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
