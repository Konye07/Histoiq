package com.yourorg.app.Data.Social

import android.content.Context
import com.yourorg.app.Data.Account.AccountRepository
import kotlinx.coroutines.delay

enum class AddFriendResult {
    ADDED, ALREADY_CONNECTED, IS_YOUR_OWN_CODE, NOT_FOUND
}

object FriendsRepository {

    /**
     * Sends a request to the given code. If it matches a demo directory entry, simulates that
     * player accepting shortly after — there's no real backend yet, so this is the only way to
     * demonstrate the full request-then-accept lifecycle from a single device.
     */
    suspend fun addFriendByCode(context: Context, rawCode: String): AddFriendResult {
        val code = rawCode.filter { it.isDigit() }
        val myAccount = AccountRepository.currentAccount(context)
        if (myAccount != null && myAccount.friendCode == code) return AddFriendResult.IS_YOUR_OWN_CODE
        if (FriendsStore.loadAll(context).any { it.code == code }) return AddFriendResult.ALREADY_CONNECTED

        val demo = FriendDirectory.find(code) ?: return AddFriendResult.NOT_FOUND
        FriendsStore.upsert(context, FriendConnection(demo.code, demo.displayName, FriendConnection.PENDING_OUTGOING))
        delay(1200)
        FriendsStore.upsert(context, FriendConnection(demo.code, demo.displayName, FriendConnection.ACCEPTED))
        return AddFriendResult.ADDED
    }

    fun acceptIncoming(context: Context, code: String, displayName: String) {
        FriendsStore.upsert(context, FriendConnection(code, displayName, FriendConnection.ACCEPTED))
    }

    fun declineIncoming(context: Context, code: String) {
        FriendsStore.remove(context, code)
    }

    fun removeFriend(context: Context, code: String) {
        FriendsStore.remove(context, code)
    }
}
