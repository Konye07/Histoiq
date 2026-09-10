package com.yourorg.app.Data.Remote

import com.yourorg.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * The app's single Supabase connection. URL and anon key come from local.properties (via
 * BuildConfig — see the buildConfigField calls in app/build.gradle.kts) so real credentials never
 * enter version control.
 *
 * Auth uses anonymous sign-in (no Google/Apple OAuth credentials exist yet, per CLAUDE.md) so every
 * device gets a real, verifiable auth.uid() that Postgres RLS policies can check — see
 * AccountRepository for how "Continue with Google/Apple" layers a display identity on top of one
 * real anonymous session instead of creating separate real-provider identities.
 */
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }
}
