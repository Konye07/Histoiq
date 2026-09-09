package com.yourorg.app.Data.Social

data class DirectoryFriend(val code: String, val displayName: String)

/**
 * Stand-in for a real backend's user directory (there's no shared server yet — every account
 * lives only on its own device). These are fixed demo players so "add a friend by code" and
 * "accept a friend request" are genuinely testable today; entering one of these codes finds a
 * real (if fake) player instead of a dead end. Swap this for a Supabase lookup by friend code
 * once accounts are shared server-side — FriendsRepository is the only caller.
 */
object FriendDirectory {
    val entries = listOf(
        DirectoryFriend("111222333", "Livia the Historian"),
        DirectoryFriend("222333444", "Marcus Quizwell"),
        DirectoryFriend("333444555", "Elena Chronicles"),
        DirectoryFriend("444555666", "Theo Mapmaker")
    )

    fun find(code: String): DirectoryFriend? = entries.find { it.code == code }
}
