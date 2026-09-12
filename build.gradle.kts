plugins {
    // 8.10.0: androidx.browser 1.10.0 (pulled in via auth-kt-android below) needs AGP 8.9.1+ and
    // compileSdk 36, but AGP 8.9.x only supports compileSdk up to 35 — 8.10.0 is the first release
    // that supports compileSdk 36, so it's the actual floor here, not just androidx.browser's stated minimum.
    // Also confirmed compatible with the Kotlin 2.3.20 pin below (compatibility range 8.2.2-9.0.0).
    id("com.android.application") version "8.10.0" apply false
    // 2.3.20, not 2.2.20: a real CI failure (macOS Kotlin/Native job) showed
    // org.jetbrains.compose.components:components-resources-iosArm64:1.11.1's klib was built by a
    // 2.3.20 compiler — a 2.2.20 compiler can only consume ABI <= 2.2.0, so it couldn't link. This
    // machine can't run the iOS Kotlin/Native compile itself (Apple targets only compile on macOS),
    // so this exact version came from CI's own error log, not a guess. Confirmed via the Kotlin
    // Multiplatform compatibility guide: Gradle 8.11.1 and AGP 8.10.0 both remain compatible with
    // Kotlin 2.3.20 (range 8.2.2-9.0.0), so no other toolchain version needed to move either.
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.3.20" apply false
    // 1.11.1, not the newer 1.12.0: 1.12.0 bumped its Android compose artifacts to require
    // compileSdk 37 + AGP 9, which this project isn't moving to as a side effect of adding KMP.
    id("org.jetbrains.compose") version "1.11.1" apply false
}
