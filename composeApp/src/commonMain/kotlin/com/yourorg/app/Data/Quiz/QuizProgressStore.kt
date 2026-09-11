package com.yourorg.app.Data.Quiz

import com.yourorg.app.Core.Platform.createSettings
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * A signed-in account's quiz progress lives in the shared `quiz_progress` table, keyed by
 * (user_id, game_key, date_key) so a resume works across devices. A guest (no account) keeps the
 * original device-local, same-CET-day settings behavior, since there's nowhere remote to put it —
 * wrapped in [GuestEnvelope] purely to carry that freshness date alongside the state.
 */
object QuizProgressStore {
    private val settings = createSettings("quiz_progress")

    suspend fun load(quizKey: String): JsonObject? {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            val raw = settings.getStringOrNull(quizKey) ?: return null
            val envelope = runCatching { Json.decodeFromString<GuestEnvelope>(raw) }.getOrNull() ?: return null
            if (envelope.date != DailyClock.todayKey()) return null
            return envelope.state
        }
        val row = fetchRemote(account.id, quizKey, DailyClock.todayKey()) ?: return null
        return row.state
    }

    suspend fun save(quizKey: String, state: JsonObject) {
        val account = AccountRepository.currentAccount()
        if (account == null) {
            val envelope = GuestEnvelope(DailyClock.todayKey(), state)
            settings.putString(quizKey, Json.encodeToString(envelope))
            return
        }
        SupabaseClient.client.postgrest.from("quiz_progress").upsert(
            QuizProgressRow(account.id, quizKey, DailyClock.todayKey(), state)
        )
    }

    /** Wipes every quiz's saved progress — device-local guest data only; a deleted account's rows cascade server-side. */
    fun clearAll() {
        settings.clear()
    }

    private suspend fun fetchRemote(userId: String, quizKey: String, dateKey: String): QuizProgressRow? =
        SupabaseClient.client.postgrest.from("quiz_progress")
            .select { filter { eq("user_id", userId); eq("game_key", quizKey); eq("date_key", dateKey) } }
            .decodeSingleOrNull<QuizProgressRow>()
}

@Serializable
private data class GuestEnvelope(val date: String, val state: JsonObject)

@Serializable
private data class QuizProgressRow(
    @SerialName("user_id") val userId: String,
    @SerialName("game_key") val gameKey: String,
    @SerialName("date_key") val dateKey: String,
    val state: JsonObject
)
