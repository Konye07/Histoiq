package com.yourorg.app.Data.Quiz

import android.content.Context
import org.json.JSONObject

/**
 * Persists one JSON blob of progress per quiz, keyed by the CET calendar day it was created on.
 * A blob from a previous day is treated as absent, which is what makes each quiz playable once
 * per day and resumable (never restartable) within that day.
 */
object QuizProgressStore {
    private const val PREFS_NAME = "quiz_progress"

    fun load(context: Context, quizKey: String): JSONObject? {
        val raw = prefs(context).getString(quizKey, null) ?: return null
        val obj = JSONObject(raw)
        if (obj.optString("date") != DailyClock.todayKey()) return null
        return obj
    }

    fun save(context: Context, quizKey: String, state: JSONObject) {
        state.put("date", DailyClock.todayKey())
        prefs(context).edit().putString(quizKey, state.toString()).apply()
    }

    /** Wipes every quiz's saved progress — used when the account is deleted. */
    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
