package com.konye.historiq.Core.Auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

/** Shared by every real ID-token provider on iOS (Google, Apple): a random raw nonce, hashed with
 * SHA-256 — the hash goes to the provider's own sign-in call, the raw value to Supabase, which
 * hashes it again itself and compares against the token's nonce claim. */
@OptIn(ExperimentalForeignApi::class)
fun generateNonce(): Pair<String, String> {
    val rawBytes = UByteArray(32)
    rawBytes.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, rawBytes.size.toULong(), pinned.addressOf(0))
    }
    val rawNonce = rawBytes.toHexString()

    val rawNonceBytes = rawNonce.encodeToByteArray()
    val digest = UByteArray(32)
    rawNonceBytes.usePinned { input ->
        digest.usePinned { output ->
            CC_SHA256(input.addressOf(0), rawNonceBytes.size.toUInt(), output.addressOf(0))
        }
    }
    return rawNonce to digest.toHexString()
}

private fun UByteArray.toHexString(): String {
    val hexChars = "0123456789abcdef"
    val builder = StringBuilder(size * 2)
    for (byte in this) {
        val v = byte.toInt()
        builder.append(hexChars[v ushr 4])
        builder.append(hexChars[v and 0x0F])
    }
    return builder.toString()
}
