package com.konye.historiq.Data.Social

import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

enum class AddFriendResult {
    ADDED, ALREADY_CONNECTED, IS_YOUR_OWN_CODE, NOT_FOUND
}

@Serializable
private data class FriendCodeLookup(val id: String, @SerialName("display_name") val displayName: String)

@Serializable
private data class FriendshipInsert(
    @SerialName("requester_id") val requesterId: String,
    @SerialName("addressee_id") val addresseeId: String,
    val status: String = "pending"
)

@Serializable
private data class FriendshipStatusUpdate(val status: String)

object FriendsRepository {

    /** Sends a real friend request by code, looked up against the shared `profiles` table. */
    suspend fun addFriendByCode(rawCode: String): AddFriendResult {
        val code = rawCode.filter { it.isDigit() }
        val myAccount = AccountRepository.currentAccount() ?: return NOT_FOUND_RESULT
        if (myAccount.friendCode == code) return AddFriendResult.IS_YOUR_OWN_CODE

        val match = SupabaseClient.client.postgrest.rpc(
            "lookup_friend_code",
            buildJsonObject { put("code", code) }
        ).decodeList<FriendCodeLookup>().firstOrNull() ?: return AddFriendResult.NOT_FOUND

        val existing = FriendsStore.loadAll(myAccount.id).any { it.otherId == match.id }
        if (existing) return AddFriendResult.ALREADY_CONNECTED

        SupabaseClient.client.postgrest.from("friendships").insert(
            FriendshipInsert(requesterId = myAccount.id, addresseeId = match.id)
        )
        return AddFriendResult.ADDED
    }

    suspend fun acceptIncoming(friendshipId: String) {
        SupabaseClient.client.postgrest.from("friendships")
            .update(FriendshipStatusUpdate("accepted")) { filter { eq("id", friendshipId) } }
    }

    suspend fun declineIncoming(friendshipId: String) {
        SupabaseClient.client.postgrest.from("friendships").delete { filter { eq("id", friendshipId) } }
    }

    suspend fun removeFriend(friendshipId: String) {
        SupabaseClient.client.postgrest.from("friendships").delete { filter { eq("id", friendshipId) } }
    }

    private val NOT_FOUND_RESULT = AddFriendResult.NOT_FOUND
}
