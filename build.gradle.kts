plugins {
    // 8.10.0: androidx.browser 1.10.0 (pulled in via auth-kt-android below) needs AGP 8.9.1+ and
    // compileSdk 36, but AGP 8.9.x only supports compileSdk up to 35 — 8.10.0 is the first release
    // that supports compileSdk 36, so it's the actual floor here, not just androidx.browser's stated minimum.
    id("com.android.application") version "8.10.0" apply false
    // Pinned to match supabase-kt 3.2.4's own build (see app/build.gradle.kts): Kotlin 2.4.0
    // emits a metadata format newer than any released Hilt/Dagger version can parse yet
    // (google/dagger#5190) — 2.2.20 is the newest Kotlin release both Hilt 2.58 and
    // supabase-kt agree on.
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20" apply false
    // 2.58, not latest: 2.59 made the Hilt Gradle plugin require AGP 9.0, and this project is on
    // AGP 8.6.1. 2.58 is the newest release before that jump, and 2.57+ already added Kotlin 2.2.x
    // metadata support (see the app/build.gradle.kts comment on the Kotlin version pin).
    id("com.google.dagger.hilt.android") version "2.58" apply false
}
