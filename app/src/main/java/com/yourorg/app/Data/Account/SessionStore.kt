package com.yourorg.app.Data.Account

import android.content.Context

/** Persists which account is currently logged in, so the app stays signed in across restarts. */
object SessionStore {
    private const val PREFS_NAME = "auth_session"
    private const val KEY_ACCOUNT_ID = "accountId"

    fun currentAccountId(context: Context): String? =
        prefs(context).getString(KEY_ACCOUNT_ID, null)

    fun setCurrentAccountId(context: Context, accountId: String?) {
        prefs(context).edit().putString(KEY_ACCOUNT_ID, accountId).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
