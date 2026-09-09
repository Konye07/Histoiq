package com.yourorg.app.Data.Remote

import com.yourorg.app.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * The app's single Supabase connection. URL and anon key come from local.properties (via
 * BuildConfig — see the buildConfigField calls in app/build.gradle.kts) so real credentials never
 * enter version control. Nothing reads or writes through this yet — Account/Quiz data still lives
 * in the local mock stores until the migration described in CLAUDE.md happens.
 *
 * auth-kt is intentionally not installed yet: its OAuth/passkey transitive dependencies need
 * AGP 8.9.1+ and compileSdk 36, a bigger upgrade than this connection-only step calls for. Add
 * `install(Auth)` (and the auth-kt dependency in build.gradle.kts) when auth actually migrates.
 */
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
    }
}
