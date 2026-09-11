package com.yourorg.app.Data.Quiz

import com.yourorg.app.Core.Platform.createSettings
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val HISTORY_DAYS_KEPT = 7

/**
 * A signed-in account's score history lives in the shared `score_history` table (full history —
 * no pruning needed once storage isn't SharedPreferences-constrained) so friends can read each
 * other's daily scores per the table's RLS policy. A guest (no account) keeps the original
 * device-local, 7-day-pruned settings-storage behavior, since there's nowhere remote to put it.
 */
object ScoreHistoryStore {
    private val settings = createSettings("score_history")

    /** Idempotent per day — recording the same day's score again is a harmless overwrite with the same value. */
    suspend fun recordResult(quizKey: String, score: Double) {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            val map = loadRaw(quizKey).toMutableMap()
            map[DailyClock.todayKey()] = score
            pruneOld(map)
            settings.putString(quizKey, Json.encodeToString(map.toMap()))
            return
        }
        SupabaseClient.client.postgrest.from("score_history").upsert(
            ScoreHistoryRow(account.id, quizKey, DailyClock.todayKey(), score)
        )
    }

    suspend fun resultFor(quizKey: String, dateKey: String): Double? {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            return loadRaw(quizKey)[dateKey]
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

    fun clearAll() {
        settings.clear()
    }

    private fun pruneOld(map: MutableMap<String, Double>) {
        val cutoff = LocalDate.parse(DailyClock.todayKey()).minus(HISTORY_DAYS_KEPT, DateTimeUnit.DAY)
        val staleKeys = map.keys.filter { key ->
            try {
                LocalDate.parse(key) < cutoff
            } catch (e: Exception) {
                false
            }
        }
        staleKeys.forEach { map.remove(it) }
    }

    /** Old org.json-era data that fails to parse under the new format is treated as "no history yet," not a crash. */
    private fun loadRaw(quizKey: String): Map<String, Double> {
        val raw = settings.getStringOrNull(quizKey) ?: return emptyMap()
        return runCatching { Json.decodeFromString<Map<String, Double>>(raw) }.getOrDefault(emptyMap())
    }
}

@Serializable
private data class ScoreHistoryRow(
    @SerialName("user_id") val userId: String,
    @SerialName("game_key") val gameKey: String,
    @SerialName("date_key") val dateKey: String,
    val score: Double
)
