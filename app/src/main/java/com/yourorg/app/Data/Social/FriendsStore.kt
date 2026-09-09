package com.yourorg.app.Data.Social

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class FriendConnection(val code: String, val displayName: String, val status: String) {
    companion object {
        const val ACCEPTED = "accepted"
        const val PENDING_INCOMING = "pending_incoming"
        const val PENDING_OUTGOING = "pending_outgoing"
    }
}

/** Local stand-in for a future Supabase "friendships" table, scoped to this device's one account. */
object FriendsStore {
    private const val PREFS_NAME = "friends"
    private const val KEY_CONNECTIONS = "connections"
    private const val KEY_SEEDED = "seededDemoIncoming"

    fun loadAll(context: Context): List<FriendConnection> {
        seedIncomingRequestOnce(context)
        val raw = prefs(context).getString(KEY_CONNECTIONS, null) ?: return emptyList()
        val array = JSONArray(raw)
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            FriendConnection(o.getString("code"), o.getString("displayName"), o.getString("status"))
        }
    }

    fun accepted(context: Context): List<FriendConnection> = loadAll(context).filter { it.status == FriendConnection.ACCEPTED }
    fun incoming(context: Context): List<FriendConnection> = loadAll(context).filter { it.status == FriendConnection.PENDING_INCOMING }
    fun outgoing(context: Context): List<FriendConnection> = loadAll(context).filter { it.status == FriendConnection.PENDING_OUTGOING }

    fun upsert(context: Context, connection: FriendConnection) {
        val list = loadAll(context).filterNot { it.code == connection.code } + connection
        writeAll(context, list)
    }

    fun remove(context: Context, code: String) {
        writeAll(context, loadAll(context).filterNot { it.code == code })
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }

    /** Demo-only: seeds one incoming request once, so the accept flow is testable without a second device. */
    private fun seedIncomingRequestOnce(context: Context) {
        if (prefs(context).getBoolean(KEY_SEEDED, false)) return
        val demo = FriendDirectory.entries.first()
        val raw = prefs(context).getString(KEY_CONNECTIONS, null)
        val array = if (raw != null) JSONArray(raw) else JSONArray()
        val entry = JSONObject()
        entry.put("code", demo.code)
        entry.put("displayName", demo.displayName)
        entry.put("status", FriendConnection.PENDING_INCOMING)
        array.put(entry)
        prefs(context).edit()
            .putString(KEY_CONNECTIONS, array.toString())
            .putBoolean(KEY_SEEDED, true)
            .apply()
    }

    private fun writeAll(context: Context, list: List<FriendConnection>) {
        val array = JSONArray()
        list.forEach { c ->
            val o = JSONObject()
            o.put("code", c.code)
            o.put("displayName", c.displayName)
            o.put("status", c.status)
            array.put(o)
        }
        prefs(context).edit().putString(KEY_CONNECTIONS, array.toString()).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
