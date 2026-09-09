package com.yourorg.app.Data.Social

import android.content.Context
import com.yourorg.app.Data.Account.AccountStore
import kotlin.random.Random

/** Generates a 9-digit code guaranteed unique among local accounts and the demo friend directory. */
fun generateUniqueFriendCode(context: Context): String {
    var candidate: String
    do {
        candidate = Random.nextInt(100_000_000, 1_000_000_000).toString()
    } while (
        AccountStore.findByFriendCode(context, candidate) != null ||
        FriendDirectory.entries.any { it.code == candidate }
    )
    return candidate
}

/** "123456789" -> "123 456 789", easier to read and to type back in. */
fun formatFriendCode(code: String): String =
    code.chunked(3).joinToString(" ")
