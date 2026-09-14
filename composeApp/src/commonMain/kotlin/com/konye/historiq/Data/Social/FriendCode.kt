package com.konye.historiq.Data.Social

import kotlin.random.Random

/**
 * A random 9-digit candidate friend code. Real uniqueness is enforced by the
 * `profiles.friend_code` unique constraint in Postgres — callers should catch a conflict and
 * retry with a new candidate rather than pre-checking locally (there's no local account list to
 * check against anymore now that profiles are shared server-side).
 */
fun randomFriendCode(): String = Random.nextInt(100_000_000, 1_000_000_000).toString()

/** "123456789" -> "123 456 789", easier to read and to type back in. */
fun formatFriendCode(code: String): String =
    code.chunked(3).joinToString(" ")
