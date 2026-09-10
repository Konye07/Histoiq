package com.yourorg.app.Data.Quiz

import android.content.Context
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.time.LocalDate

private const val HISTORY_DAYS_KEPT = 7L

/**
 * A signed-in account's score history lives in the shared `score_history` table (full history —
 * no pruning needed once storage isn't SharedPreferences-constrained) so friends can read each
 * other's daily scores per the table's RLS policy. A guest (no account) keeps the original
 * device-local, 7-day-pruned SharedPreferences behavior, since there's nowhere remote to put it.
 */
object ScoreHistoryStore {
    private const val PREFS_NAME = "score_history"

    /** Idempotent per day — recording the same day's score again is a harmless overwrite with the same value. */
    suspend fun recordResult(context: Context, quizKey: String, score: Double) {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            val obj = loadRaw(context, quizKey)
            obj.put(DailyClock.todayKey(), score)
            pruneOld(obj)
            prefs(context).edit().putString(quizKey, obj.toString()).apply()
            return
        }
        SupabaseClient.client.postgrest.from("score_history").upsert(
            ScoreHistoryRow(account.id, quizKey, DailyClock.todayKey(), score)
        )
    }

    suspend fun resultFor(context: Context, quizKey: String, dateKey: String): Double? {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            val obj = loadRaw(context, quizKey)
            return if (obj.has(dateKey)) obj.getDouble(dateKey) else null
        }
        return SupabaseClient.client.postgrest.from("score_history")
            .select {
                filter {
                    eq("user_id", account.id)
                    eq("game_key", quizKey)
                    eq("date_key", dateKey)
                }
            }
            .decodeSingleOrNull<ScoreHistoryRow>()?.score
    }

    /** Any other signed-in user's score for a game/day — relies on score_history's friend-visible RLS policy. */
    suspend fun resultFor(otherUserId: String, quizKey: String, dateKey: String): Double? =
        SupabaseClient.client.postgrest.from("score_history")
            .select {
                filter {
                    eq("user_id", otherUserId)
                    eq("game_key", quizKey)
                    eq("date_key", dateKey)
                }
            }
            .decodeSingleOrNull<ScoreHistoryRow>()?.score

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun pruneOld(obj: JSONObject) {
        val cutoff = LocalDate.parse(DailyClock.todayKey()).minusDays(HISTORY_DAYS_KEPT)
        val staleKeys = obj.keys().asSequence().filter { key ->
            try {
                LocalDate.parse(key) < cutoff
            } catch (e: Exception) {
                false
            }
        }.toList()
        staleKeys.forEach { obj.remove(it) }
    }

    private fun loadRaw(context: Context, quizKey: String): JSONObject {
        val raw = prefs(context).getString(quizKey, null) ?: return JSONObject()
        return JSONObject(raw)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

@Serializable
private data class ScoreHistoryRow(
    @SerialName("user_id") val userId: String,
    @SerialName("game_key") val gameKey: String,
    @SerialName("date_key") val dateKey: String,
    val score: Double
)
