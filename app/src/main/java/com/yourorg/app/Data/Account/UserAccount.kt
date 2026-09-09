package com.yourorg.app.Data.Account

/** Local mirror of what will become a Supabase "accounts" table row. */
data class UserAccount(
    val id: String,
    val provider: String,
    val providerUserId: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: Long,
    /** Unique 9-digit code other players use to add this account as a friend. */
    val friendCode: String
)
