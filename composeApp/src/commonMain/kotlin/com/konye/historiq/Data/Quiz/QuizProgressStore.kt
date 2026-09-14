package com.konye.historiq.Data.Quiz

import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Every device's quiz progress lives in the shared `quiz_progress` table, keyed by (user_id,
 * game_key, date_key) under a real auth.uid() — a guest gets one too via
 * [AccountRepository.ensureAnonymousSession], not just a signed-in account. That's what makes
 * "once per game per day" a real, server-enforced guarantee for a guest rather than a
 * device-local flag that clearing app storage would reset — the same guarantee a signed-in
 * account already had. `quiz_progress`'s foreign key points at auth.users, not profiles,
 * specifically so this works without a profile/display identity.
 */
object QuizProgressStore {
    suspend fun load(quizKey: String): JsonObject? {
        val userId = AccountRepository.ensureAnonymousSession()
        return fetchRemote(userId, quizKey, DailyClock.todayKey())?.state
    }

    suspend fun save(quizKey: String, state: JsonObject) {
        val userId = AccountRepository.ensureAnonymousSession()
        SupabaseClient.client.postgrest.from("quiz_progress").upsert(
            QuizProgressRow(userId, quizKey, DailyClock.todayKey(), state)
        )
    }

    private suspend fun fetchRemote(userId: String, quizKey: String, dateKey: String): QuizProgressRow? =
        SupabaseClient.client.postgrest.from("quiz_progress")
            .select { filter { eq("user_id", userId); eq("game_key", quizKey); eq("date_key", dateKey) } }
            .decodeSingleOrNull<QuizProgressRow>()
}

@Serializable
private data class QuizProgressRow(
    @SerialName("user_id") val userId: String,
    @SerialName("game_key") val gameKey: String,
    @SerialName("date_key") val dateKey: String,
    val state: JsonObject
)
