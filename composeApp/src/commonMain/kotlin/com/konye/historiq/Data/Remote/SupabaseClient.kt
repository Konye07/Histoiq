package com.konye.historiq.Data.Remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/** The deep-link scheme+host the OAuth-redirect flow (Core/Auth/FacebookSignIn.*.kt) redirects
 * back to once a provider (Facebook) completes sign-in in the browser — registered as an intent
 * filter (Android) / URL scheme (iOS), and must also be added to Supabase's own Auth > URL
 * Configuration > Redirect URLs allow-list. Not used by Google/Apple, which sign in via their own
 * native SDKs instead. */
const val OAUTH_REDIRECT_SCHEME = "com.konye.historiq"
const val OAUTH_REDIRECT_HOST = "login-callback"

/**
 * The app's single Supabase connection. URL and anon key come from local.properties (via the
 * generated SupabaseConfig object — see composeApp/build.gradle.kts) so real credentials never
 * enter version control.
 *
 * Auth uses anonymous sign-in so every device gets a real, verifiable auth.uid() that Postgres RLS
 * policies can check; Google/Apple attach a real identity via their own native id-token SDKs, and
 * Facebook via the browser-redirect OAuth flow below (see FacebookSignIn.*.kt for why: Facebook's
 * Android SDK only mints a real id-token on an account's first-ever authorization — a confirmed,
 * unfixed SDK limitation, facebook/facebook-android-sdk#1132).
 */
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = SupabaseConfig.URL,
        supabaseKey = SupabaseConfig.ANON_KEY
    ) {
        install(Postgrest)
        install(Auth) {
            flowType = FlowType.PKCE
            scheme = OAUTH_REDIRECT_SCHEME
            host = OAUTH_REDIRECT_HOST
        }
    }
}
