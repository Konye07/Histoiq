plugins {
    // 8.10.0: androidx.browser 1.10.0 (pulled in via auth-kt-android below) needs AGP 8.9.1+ and
    // compileSdk 36, but AGP 8.9.x only supports compileSdk up to 35 — 8.10.0 is the first release
    // that supports compileSdk 36, so it's the actual floor here, not just androidx.browser's stated minimum.
    id("com.android.application") version "8.10.0" apply false
    // Pinned to match supabase-kt 3.2.4's own build (see app/build.gradle.kts) rather than latest —
    // no functional need to track newer, and this exact combination is verified working.
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20" apply false
}
