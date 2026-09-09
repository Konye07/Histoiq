package com.yourorg.app.Data.Quiz

import android.content.Context
import org.json.JSONObject

/**
 * Remembers which item ids were used per game over the last [COOLDOWN_DAYS] days, so daily
 * selection can avoid repeating a battle/flag/statement/etc. too soon. Distinct from
 * [QuizProgressStore], which only ever cares about the current CET day.
 */
object RecentlyUsedStore {
    private const val COOLDOWN_DAYS = 15
    private const val PREFS_NAME = "recently_used_items"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadMap(context: Context, gameKey: String): MutableMap<String, String> {
        val raw = prefs(context).getString(gameKey, null) ?: return mutableMapOf()
        val obj = JSONObject(raw)
        val cutoff = DailyClock.dateKey(COOLDOWN_DAYS)
        val map = mutableMapOf<String, String>()
        for (key in obj.keys()) {
            val date = obj.getString(key)
            if (date > cutoff) map[key] = date
        }
        return map
    }

    private fun saveMap(context: Context, gameKey: String, map: Map<String, String>) {
        val obj = JSONObject()
        for ((id, date) in map) obj.put(id, date)
        prefs(context).edit().putString(gameKey, obj.toString()).apply()
    }

    fun recentlyUsedIds(context: Context, gameKey: String): Set<String> =
        loadMap(context, gameKey).keys

    /** Marks [ids] as used today for [gameKey]; call once per id, right when it's actually shown. */
    fun record(context: Context, gameKey: String, ids: List<String>) {
        if (ids.isEmpty()) return
        val map = loadMap(context, gameKey)
        val today = DailyClock.todayKey()
        for (id in ids) map[id] = today
        saveMap(context, gameKey, map)
    }

    /**
     * Picks [n] random items from [pool], excluding ones used in the last 15 days, and records
     * the chosen ones as used today. Falls back to the unfiltered pool if fewer than [n] items
     * would remain, so a small data pool never runs out of eligible questions.
     */
    fun <T> pickDaily(context: Context, gameKey: String, pool: List<T>, n: Int, idOf: (T) -> String): List<T> {
        val source = eligiblePool(context, gameKey, pool, n, idOf)
        val chosen = source.shuffled().take(n)
        record(context, gameKey, chosen.map(idOf))
        return chosen
    }

    /**
     * Filters [pool] down to items not used in the last 15 days, falling back to the full pool
     * when fewer than [minRemaining] would be left. For games like Territory Compare that draw
     * continuously from a queue rather than picking a fixed N up front.
     */
    fun <T> eligiblePool(context: Context, gameKey: String, pool: List<T>, minRemaining: Int, idOf: (T) -> String): List<T> {
        val recentlyUsed = recentlyUsedIds(context, gameKey)
        val eligible = pool.filter { idOf(it) !in recentlyUsed }
        return if (eligible.size >= minRemaining) eligible else pool
    }
}
