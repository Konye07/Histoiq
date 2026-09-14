package com.konye.historiq.Data.Remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * The app's single Supabase connection. URL and anon key come from local.properties (via the
 * generated SupabaseConfig object — see composeApp/build.gradle.kts) so real credentials never
 * enter version control.
 *
 * Auth uses anonymous sign-in (no Google/Apple OAuth credentials exist yet, per CLAUDE.md) so every
 * device gets a real, verifiable auth.uid() that Postgres RLS policies can check.
 */
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = SupabaseConfig.URL,
        supabaseKey = SupabaseConfig.ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }
}
