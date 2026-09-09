package com.yourorg.app.Data.Quiz

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate

private const val HISTORY_DAYS_KEPT = 7L

/** Keeps each quiz's final score per calendar day (bounded window), so Friends can compare past days. */
object ScoreHistoryStore {
    private const val PREFS_NAME = "score_history"

    /** Idempotent per day — recording the same day's score again is a harmless overwrite with the same value. */
    fun recordResult(context: Context, quizKey: String, score: Double) {
        val obj = loadRaw(context, quizKey)
        obj.put(DailyClock.todayKey(), score)
        pruneOld(obj)
        prefs(context).edit().putString(quizKey, obj.toString()).apply()
    }

    fun resultFor(context: Context, quizKey: String, dateKey: String): Double? {
        val obj = loadRaw(context, quizKey)
        return if (obj.has(dateKey)) obj.getDouble(dateKey) else null
    }

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
