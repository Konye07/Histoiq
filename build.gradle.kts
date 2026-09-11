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
    // KMP migration (composeApp module): Kotlin 2.2.20 is verified compatible with Compose
    // Multiplatform 1.12.0 and with this project's existing Gradle 8.11.1 / AGP 8.10.0 pins, so no
    // other toolchain version needed to move.
    id("org.jetbrains.kotlin.multiplatform") version "2.2.20" apply false
    // 1.11.1, not the newer 1.12.0: 1.12.0 bumped its Android compose artifacts to require
    // compileSdk 37 + AGP 9, which this project isn't moving to as a side effect of adding KMP.
    // 1.11.x is on the same Kotlin 2.2 language/API version line and stays on compileSdk 36/AGP 8.x.
    id("org.jetbrains.compose") version "1.11.1" apply false
}
