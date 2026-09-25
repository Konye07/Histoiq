package com.konye.historiq.Data.Quiz

import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The single source of truth for "today's quiz" for one game — replaces RecentlyUsedStore's
 * per-device random pick. The first player of the day to ask triggers get_or_create_daily_quiz
 * (a Postgres function) to assign it once, atomically, including the repeat cooldown that used to
 * live in RecentlyUsedStore (per-game now — 15 days by default, shorter for a few games where
 * either the catalog can't support 15 days or it's just a deliberate choice; see that function's
 * own comment for the exact per-game values, not duplicated here since it's the only place that
 * should ever need editing); everyone after just reads the same row, so every device sees the
 * same questions on the same date.
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
