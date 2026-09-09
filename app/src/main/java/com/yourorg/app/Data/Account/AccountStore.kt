package com.yourorg.app.Data.Account

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * A local stand-in for the future Supabase "accounts" table: every registered account as a JSON
 * array in the app's own internal storage (accounts.json). Schema mirrors what will move to
 * Supabase — id, provider, providerUserId, email, displayName, avatarUrl, createdAt — so the
 * migration is a matter of pointing these same reads/writes at a Supabase table.
 */
object AccountStore {
    private const val FILE_NAME = "accounts.json"

    fun loadAll(context: Context): List<UserAccount> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            UserAccount(
                id = o.getString("id"),
                provider = o.getString("provider"),
                providerUserId = o.getString("providerUserId"),
                email = o.getString("email"),
                displayName = o.getString("displayName"),
                avatarUrl = o.optString("avatarUrl", "").ifBlank { null },
                createdAt = o.getLong("createdAt"),
                friendCode = o.optString("friendCode", "")
            )
        }
    }

    fun findByProviderIdentity(context: Context, provider: String, providerUserId: String): UserAccount? =
        loadAll(context).find { it.provider == provider && it.providerUserId == providerUserId }

    fun findById(context: Context, id: String): UserAccount? =
        loadAll(context).find { it.id == id }

    fun findByFriendCode(context: Context, friendCode: String): UserAccount? =
        loadAll(context).find { it.friendCode == friendCode }

    fun save(context: Context, account: UserAccount) {
        val existing = loadAll(context).filterNot { it.id == account.id }
        writeAll(context, existing + account)
    }

    fun delete(context: Context, id: String) {
        writeAll(context, loadAll(context).filterNot { it.id == id })
    }

    private fun writeAll(context: Context, accounts: List<UserAccount>) {
        val array = JSONArray()
        accounts.forEach { a ->
            val o = JSONObject()
            o.put("id", a.id)
            o.put("provider", a.provider)
            o.put("providerUserId", a.providerUserId)
            o.put("email", a.email)
            o.put("displayName", a.displayName)
            o.put("avatarUrl", a.avatarUrl ?: "")
            o.put("createdAt", a.createdAt)
            o.put("friendCode", a.friendCode)
            array.put(o)
        }
        File(context.filesDir, FILE_NAME).writeText(array.toString())
    }
}
