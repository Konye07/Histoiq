package com.konye.historiq.Core.Network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Runs a best-effort server call, returning [fallback] instead of throwing when the request fails. */
suspend fun <T> networkOrElse(fallback: T, block: suspend () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        println("Historiq: network call failed, using fallback — ${e::class.simpleName}: ${e.message}")
        fallback
    }

/** Retries [block] until it succeeds, for work a screen can't proceed without; stops with the calling composition. */
suspend fun <T> retryNetwork(retryDelayMs: Long = 2_000, block: suspend () -> T): T {
    while (true) {
        try {
            return block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("Historiq: network call failed, retrying in ${retryDelayMs}ms — ${e::class.simpleName}: ${e.message}")
            delay(retryDelayMs)
        }
    }
}
