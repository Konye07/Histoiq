package com.yourorg.app.Data.Account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors a row of the Supabase "profiles" table (id = the real auth.uid() of an anonymous session). */
@Serializable
data class UserAccount(
    val id: String,
    val provider: String,
    @SerialName("provider_user_id") val providerUserId: String,
    val email: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    /** Unique 9-digit code other players use to add this account as a friend. */
    @SerialName("friend_code") val friendCode: String,
    @SerialName("created_at") val createdAt: String? = null
)
