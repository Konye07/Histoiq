package com.konye.historiq.Data.Quiz

import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The single source of truth for "today's quiz" for one game — replaces RecentlyUsedStore's
 * per-device random pick. The first player of the day to ask triggers get_or_create_daily_quiz
 * (a Postgres function) to assign it once, atomically, including the 15-day cooldown that used to
 * live in RecentlyUsedStore; everyone after just reads the same row, so every device sees the same
 * questions on the same date.
 */
object DailyQuizRepository {
    suspend fun getOrCreate(gameKey: String, dateKey: String, count: Int): List<String> =
        SupabaseClient.client.postgrest.rpc(
            "get_or_create_daily_quiz",
            buildJsonObject {
                put("p_game_key", gameKey)
                put("p_date", dateKey)
                put("p_count", count)
            }
        ).decodeAs()
}
