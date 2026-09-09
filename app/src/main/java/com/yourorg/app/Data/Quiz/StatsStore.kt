package com.yourorg.app.Data.Quiz

import android.content.Context
import org.json.JSONObject

data class GameStats(val gamesPlayed: Int, val averageScore: Double, val perfectCount: Int)

/** Lifetime per-quiz totals (games played, average score, perfect runs) — separate from the bounded score history. */
object StatsStore {
    private const val PREFS_NAME = "game_stats"

    /** Idempotent per day: a quiz that's already been recorded for today won't be double-counted. */
    fun recordCompletion(context: Context, quizKey: String, score: Double, isPerfect: Boolean) {
        val today = DailyClock.todayKey()
        val obj = loadRaw(context, quizKey)
        if (obj.optString("lastRecordedDate") == today) return

        obj.put("gamesPlayed", obj.optInt("gamesPlayed", 0) + 1)
        obj.put("totalScore", obj.optDouble("totalScore", 0.0) + score)
        obj.put("perfectCount", obj.optInt("perfectCount", 0) + if (isPerfect) 1 else 0)
        obj.put("lastRecordedDate", today)
        prefs(context).edit().putString(quizKey, obj.toString()).apply()
    }

    fun statsFor(context: Context, quizKey: String): GameStats {
        val obj = loadRaw(context, quizKey)
        val gamesPlayed = obj.optInt("gamesPlayed", 0)
        val totalScore = obj.optDouble("totalScore", 0.0)
        val perfectCount = obj.optInt("perfectCount", 0)
        val average = if (gamesPlayed > 0) totalScore / gamesPlayed else 0.0
        return GameStats(gamesPlayed, average, perfectCount)
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun loadRaw(context: Context, quizKey: String): JSONObject {
        val raw = prefs(context).getString(quizKey, null) ?: return JSONObject()
        return JSONObject(raw)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
