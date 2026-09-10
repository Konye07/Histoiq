package com.yourorg.app.Data.Social

import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class FriendshipRow(
    val id: String,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("addressee_id") val addresseeId: String,
    val status: String
)

@Serializable
private data class ProfileNameRow(val id: String, @SerialName("display_name") val displayName: String)

/** One real friendship row, from the current account's point of view. */
data class FriendConnection(
    val friendshipId: String,
    val otherId: String,
    val displayName: String,
    val status: String,
    val iAmRequester: Boolean
) {
    companion object {
        const val ACCEPTED = "accepted"
        const val PENDING = "pending"
    }
}

/** Real friendships backed by the shared `friendships`/`profiles` tables (RLS-scoped to the caller). */
object FriendsStore {
    suspend fun loadAll(myId: String): List<FriendConnection> {
        val rows = SupabaseClient.client.postgrest.from("friendships")
            .select {
                filter {
                    or {
                        eq("requester_id", myId)
                        eq("addressee_id", myId)
                    }
                }
            }
            .decodeList<FriendshipRow>()
        if (rows.isEmpty()) return emptyList()

        val otherIds = rows.map { if (it.requesterId == myId) it.addresseeId else it.requesterId }.distinct()
        val names = SupabaseClient.client.postgrest.from("profiles")
            .select { filter { isIn("id", otherIds) } }
            .decodeList<ProfileNameRow>()
            .associateBy({ it.id }, { it.displayName })

        return rows.map { row ->
            val iAmRequester = row.requesterId == myId
            val otherId = if (iAmRequester) row.addresseeId else row.requesterId
            FriendConnection(row.id, otherId, names[otherId] ?: "Unknown", row.status, iAmRequester)
        }
    }

    suspend fun accepted(myId: String): List<FriendConnection> =
        loadAll(myId).filter { it.status == FriendConnection.ACCEPTED }

    /** Requests sent TO me that I haven't responded to yet. */
    suspend fun incoming(myId: String): List<FriendConnection> =
        loadAll(myId).filter { it.status == FriendConnection.PENDING && !it.iAmRequester }
}
