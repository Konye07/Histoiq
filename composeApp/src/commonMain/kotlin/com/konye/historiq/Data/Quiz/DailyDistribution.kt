package com.konye.historiq.Data.Quiz

import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlin.math.roundToInt

/**
 * Real "how did today's other players do" data — replaces the old seeded-random synthetic
 * generator now that real scores/guesses exist in score_history/quiz_progress. Two Postgres
 * functions do the reading (get_final_scores, get_question_guesses): both are SECURITY DEFINER
 * and return only anonymized aggregate values, never which user made them, since
 * quiz_progress/score_history's RLS is otherwise owner/friend-only by design.
 */
object DailyDistribution {
    /** Every player's final score for a game today — its size is also "total players today". */
    suspend fun finalScores(gameKey: String, dateKey: String): List<Double> =
        SupabaseClient.client.postgrest.rpc(
            "get_final_scores",
            buildJsonObject { put("p_game_key", gameKey); put("p_date", dateKey) }
        ).decodeAs()

    /**
     * (rank, totalPlayers, topPercent) for one game/day given the player's value and today's real
     * score distribution — topPercent is "you beat N% of today's *other* players", always shown
     * alongside the exact rank on every results screen. [samples] is the full today's-scores list
     * as returned by [finalScores], which includes the caller's own just-recorded row (recordAndRank
     * writes it, then reads it straight back) — that row is excluded before computing topPercent,
     * otherwise beating everyone else still couldn't reach 100% (e.g. beating both of 2 others
     * landed on 2/3 = 67%, counting yourself as a third player you'd have to beat). totalPlayers
     * itself keeps counting the caller, since "3 players today" (including you) is the correct
     * headline number — only the *comparison* excludes you.
     */
    fun rankAmongPlayers(value: Double, samples: List<Double>, higherIsBetter: Boolean): Triple<Int, Int, Int> {
        val totalPlayers = samples.size.coerceAtLeast(1)
        val others = samples.toMutableList().also { it.remove(value) }
        if (others.isEmpty()) return Triple(1, totalPlayers, 100)
        val beaten = others.count { if (higherIsBetter) it < value else it > value }
        val topPercent = ((beaten * 100.0) / others.size).roundToInt().coerceIn(0, 100)
        val rank = (others.size - beaten + 1).coerceIn(1, totalPlayers)
        return Triple(rank, totalPlayers, topPercent)
    }

    /** Percent of [otherDistances] farther from correct than [yourDistance] (lower distance is always better). */
    fun closerThanPercent(yourDistance: Double, otherDistances: List<Double>): Int {
        if (otherDistances.isEmpty()) return 100
        val beaten = otherDistances.count { it > yourDistance }
        return ((beaten * 100.0) / otherDistances.size).roundToInt().coerceIn(0, 100)
    }

    suspend fun yearGuesses(gameKey: String, dateKey: String, questionId: String): List<Int> =
        rawGuesses(gameKey, dateKey, questionId).mapNotNull { it.jsonPrimitive.longOrNull?.toInt() }

    suspend fun casualtyGuesses(gameKey: String, dateKey: String, questionId: String): List<Long> =
        rawGuesses(gameKey, dateKey, questionId).mapNotNull { it.jsonPrimitive.longOrNull }

    private suspend fun rawGuesses(gameKey: String, dateKey: String, questionId: String): List<JsonElement> =
        SupabaseClient.client.postgrest.rpc(
            "get_question_guesses",
            buildJsonObject {
                put("p_game_key", gameKey)
                put("p_date", dateKey)
                put("p_question_id", questionId)
            }
        ).decodeAs()
}
