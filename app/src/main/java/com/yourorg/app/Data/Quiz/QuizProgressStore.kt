package com.yourorg.app.Data.Quiz

import android.content.Context
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.json.JSONObject

/**
 * A signed-in account's quiz progress lives in the shared `quiz_progress` table, keyed by
 * (user_id, game_key, date_key) so a resume works across devices. A guest (no account) keeps the
 * original device-local, same-CET-day SharedPreferences behavior, since there's nowhere remote to
 * put it.
 */
object QuizProgressStore {
    private const val PREFS_NAME = "quiz_progress"

    suspend fun load(context: Context, quizKey: String): JSONObject? {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            val raw = prefs(context).getString(quizKey, null) ?: return null
            val obj = JSONObject(raw)
            if (obj.optString("date") != DailyClock.todayKey()) return null
            return obj
        }
        val row = fetchRemote(account.id, quizKey, DailyClock.todayKey()) ?: return null
        return JSONObject(row.state.toString())
    }

    suspend fun save(context: Context, quizKey: String, state: JSONObject) {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            state.put("date", DailyClock.todayKey())
            prefs(context).edit().putString(quizKey, state.toString()).apply()
            return
        }
        val jsonState = Json.parseToJsonElement(state.toString()) as JsonObject
        SupabaseClient.client.postgrest.from("quiz_progress").upsert(
            QuizProgressRow(account.id, quizKey, DailyClock.todayKey(), jsonState)
        )
    }

    /** Wipes every quiz's saved progress — device-local guest data only; a deleted account's rows cascade server-side. */
    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private suspend fun fetchRemote(userId: String, quizKey: String, dateKey: String): QuizProgressRow? =
        SupabaseClient.client.postgrest.from("quiz_progress")
            .select { filter { eq("user_id", userId); eq("game_key", quizKey); eq("date_key", dateKey) } }
            .decodeSingleOrNull<QuizProgressRow>()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

@Serializable
private data class QuizProgressRow(
    @SerialName("user_id") val userId: String,
    @SerialName("game_key") val gameKey: String,
    @SerialName("date_key") val dateKey: String,
    val state: JsonObject
)
