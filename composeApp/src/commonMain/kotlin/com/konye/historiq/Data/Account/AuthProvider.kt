package com.konye.historiq.Data.Account

import kotlinx.coroutines.delay

enum class AuthProviderType(val id: String, val label: String) {
    GOOGLE("google", "Google"),
    APPLE("apple", "Apple"),
    FACEBOOK("facebook", "Facebook")
}

data class AuthResult(
    val providerUserId: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null
)

/**
 * Stands in for a real Google Credential Manager / Sign in with Apple call. Both need OAuth
 * credentials this project doesn't have yet (a Google Cloud OAuth client ID; an Apple Services ID
 * plus a backend to verify its signed token), so this just simulates the round trip and hands back
 * a stable mock identity per provider. Swap this function's body for the real SDK call later —
 * everything downstream (AccountRepository, session, UI) is written against AuthResult and won't
 * need to change.
 */
suspend fun mockSignIn(provider: AuthProviderType): AuthResult {
    delay(500)
    return when (provider) {
        AuthProviderType.GOOGLE -> AuthResult(
            providerUserId = "mock-google-user-1",
            email = "player@gmail.com",
            displayName = "Google Player"
        )
        AuthProviderType.APPLE -> AuthResult(
            providerUserId = "mock-apple-user-1",
            email = "player@icloud.com",
            displayName = "Apple Player"
        )
        AuthProviderType.FACEBOOK -> AuthResult(
            providerUserId = "mock-facebook-user-1",
            email = "player@facebook.com",
            displayName = "Facebook Player"
        )
    }
}
