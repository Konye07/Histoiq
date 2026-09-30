package com.konye.historiq.Core.Auth

import java.security.MessageDigest
import java.security.SecureRandom

/** Shared by every real ID-token provider on Android (Google, Facebook): a random raw nonce,
 * hashed with SHA-256 — the hash goes to the provider's own sign-in call, the raw value to
 * Supabase, which hashes it again itself and compares against the token's nonce claim. */
fun generateNonce(): Pair<String, String> {
    val rawBytes = ByteArray(32)
    SecureRandom().nextBytes(rawBytes)
    val rawNonce = rawBytes.joinToString("") { "%02x".format(it) }
    val hashedNonce = MessageDigest.getInstance("SHA-256")
        .digest(rawNonce.toByteArray())
        .joinToString("") { "%02x".format(it) }
    return rawNonce to hashedNonce
}
