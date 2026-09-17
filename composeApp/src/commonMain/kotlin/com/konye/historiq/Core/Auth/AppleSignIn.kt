package com.konye.historiq.Core.Auth

import com.konye.historiq.Data.Account.AuthResult

sealed class AppleAuthOutcome {
    data class SignedIn(val result: AuthResult) : AppleAuthOutcome()
    object CancelledOrFailed : AppleAuthOutcome()
    object NotImplementedOnThisPlatform : AppleAuthOutcome()
}

expect suspend fun signInWithApple(): AppleAuthOutcome
