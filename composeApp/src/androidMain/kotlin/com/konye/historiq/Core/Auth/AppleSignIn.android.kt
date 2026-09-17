package com.konye.historiq.Core.Auth

/** Android hides the "Continue with Apple" button entirely (see [showAppleSignInButton]) — this is never reachable. */
actual suspend fun signInWithApple(): AppleAuthOutcome = AppleAuthOutcome.NotImplementedOnThisPlatform
