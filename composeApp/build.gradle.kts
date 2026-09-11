import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.compose")
}

/**
 * Loads SUPABASE_URL / SUPABASE_ANON_KEY from local.properties (gitignored) and writes them into a
 * generated commonMain object — the multiplatform replacement for `app/build.gradle.kts`'s
 * `buildConfigField` calls, since Android's BuildConfig mechanism doesn't exist for the iOS target.
 * Debug and release share identical Supabase config today (no per-build-type branching existed
 * before this migration either), so one generated object for all targets is a faithful port.
 */
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun localProperty(key: String): String = localProperties.getProperty(key, "")

val generatedConfigDir = layout.buildDirectory.dir("generated/supabaseConfig/commonMain/kotlin").get().asFile
generatedConfigDir.mkdirs()
File(generatedConfigDir, "SupabaseConfig.kt").writeText(
    """
    package com.yourorg.app.Data.Remote

    /** Generated at Gradle configuration time from local.properties — never commit real values. */
    internal object SupabaseConfig {
        const val URL = "${localProperty("SUPABASE_URL")}"
        const val ANON_KEY = "${localProperty("SUPABASE_ANON_KEY")}"
    }
    """.trimIndent()
)

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // No iosX64() (Intel simulator): current Compose Multiplatform / JetBrains navigation
    // artifacts publish only iosArm64 (device) + iosSimulatorArm64 (Apple Silicon simulator)
    // variants — confirmed by a real dependency-resolution failure that listed every other iOS
    // variant as a near-miss except iosX64, which had no variant at all. Virtually all current
    // Macs are Apple Silicon, so this isn't a practical loss.
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generatedConfigDir)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                // NOT compose.materialIconsExtended: that artifact is permanently pinned to Compose
                // 1.7.3 (a pre-iOS-stability release) and won't resolve for the iOS target at all —
                // JetBrains' own deprecation note recommends Material Symbols (vector resources)
                // instead. Revisit when icons are actually needed (Phase 6 UI port).
                implementation(compose.ui)
                implementation(compose.components.resources)
                // NOT Google's androidx.navigation:navigation-compose — that artifact's transitive
                // lifecycle-viewmodel-ktx dependency is Android/JVM-only and fails iOS resolution
                // (confirmed by running compileIosMainKotlinMetadata). JetBrains publishes its own
                // properly cross-published coordinates for Compose Multiplatform under
                // org.jetbrains.androidx.navigation instead.
                // Not 2.10.x: that line's Android artifact requires compileSdk 37/AGP 9 (confirmed
                // by a real checkDebugAarMetadata failure), the same wall CMP 1.12.0 hits — staying
                // on 2.9.0-beta03 (contemporary with Compose 1.8.2, before that requirement wave)
                // keeps this on compileSdk 36/AGP 8.10.0 like the rest of the project.
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta03")

                // Pinned to 3.2.4 to match the Kotlin 2.2.20 pin — see root build.gradle.kts.
                // Declared directly (not via the BOM) since `platform(...)` inside a KMP source
                // set's `dependencies {}` block hits KT-58759 (an ambiguous-overload compile error)
                // with this Kotlin/AGP combination.
                implementation("io.github.jan-tennert.supabase:postgrest-kt:3.2.4")
                implementation("io.github.jan-tennert.supabase:auth-kt:3.2.4")

                // Declared explicitly now rather than riding in transitively via Supabase, since the
                // data-layer stores call these directly (kotlinx.serialization already did this
                // implicitly in the old `app` module; kotlinx-datetime is new, needed to replace
                // java.time in DailyClock.kt, which doesn't exist on Kotlin/Native).
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.2")
                implementation("androidx.core:core-ktx:1.13.1")
                implementation("io.ktor:ktor-client-android:3.3.0")
            }
        }
        // iosMain is auto-created (and already dependsOn(commonMain) / has iosArm64Main and
        // iosSimulatorArm64Main depend on it) by Kotlin's default hierarchy template once iOS
        // targets are declared above — no manual dependsOn wiring needed or wanted here.
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.3.0")
            }
        }
    }
}

android {
    namespace = "com.yourorg.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yourorg.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
