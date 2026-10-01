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
    package com.konye.historiq.Data.Remote

    /** Generated at Gradle configuration time from local.properties — never commit real values. */
    internal object SupabaseConfig {
        const val URL = "${localProperty("SUPABASE_URL")}"
        const val ANON_KEY = "${localProperty("SUPABASE_ANON_KEY")}"
    }
    """.trimIndent()
)

/**
 * AdMob App ID + ad unit IDs: same local.properties/generated-object pattern as Supabase above.
 * Defaults to Google's own public test IDs (safe to ship, always fill, never charge real
 * advertisers) when the ADMOB_ keys aren't set in local.properties — real values are set there
 * once a real AdMob account/app/ad units exist.
 */
fun localPropertyOrDefault(key: String, default: String): String =
    localProperty(key).ifBlank { default }

val admobAppId = localPropertyOrDefault("ADMOB_APP_ID_ANDROID", "ca-app-pub-3940256099942544~3347511713")
val admobInterstitialUnitId = localPropertyOrDefault("ADMOB_INTERSTITIAL_UNIT_ID_ANDROID", "ca-app-pub-3940256099942544/1033173712")
val admobRewardedUnitId = localPropertyOrDefault("ADMOB_REWARDED_UNIT_ID_ANDROID", "ca-app-pub-3940256099942544/5224354917")

// iOS ad unit IDs (Core/Ads/InterstitialAd.ios.kt, RewardedAd.ios.kt) — separate constants from the
// Android ones above (never reused for both platforms, since real AdMob ad units are always
// platform-specific). Defaults are Google's public test IDs for iOS, see
// https://developers.google.com/admob/ios/test-ads. Replace ADMOB_INTERSTITIAL_UNIT_ID_IOS/
// ADMOB_REWARDED_UNIT_ID_IOS in local.properties once a real iOS AdMob app/ad units exist.
val admobInterstitialUnitIdIos = localPropertyOrDefault("ADMOB_INTERSTITIAL_UNIT_ID_IOS", "ca-app-pub-3940256099942544/4411468910")
val admobRewardedUnitIdIos = localPropertyOrDefault("ADMOB_REWARDED_UNIT_ID_IOS", "ca-app-pub-3940256099942544/1712485313")

// Blank by default (no-op in CI/release) — set to a real device's hashed test-device ID (printed to
// logcat/console on first run with no debug settings) to force the UMP consent form to appear as if
// the device were in the EEA, since otherwise testing it requires physically being there. Android
// only: see Core/Ads/ConsentManager.android.kt.
val admobDebugGeoEeaTestDeviceId = localPropertyOrDefault("ADMOB_DEBUG_GEO_EEA_TEST_DEVICE_ID", "")

File(generatedConfigDir, "AdsConfig.kt").writeText(
    """
    package com.konye.historiq.Core.Ads

    /**
     * Generated at Gradle configuration time from local.properties. Defaults are Google's public
     * test IDs (see https://developers.google.com/admob/android/test-ads and
     * https://developers.google.com/admob/ios/test-ads) — real (test-only) ads that never charge
     * advertisers, safe to ship. Replace ADMOB_APP_ID_ANDROID/ADMOB_INTERSTITIAL_UNIT_ID_ANDROID/
     * ADMOB_REWARDED_UNIT_ID_ANDROID/ADMOB_INTERSTITIAL_UNIT_ID_IOS/ADMOB_REWARDED_UNIT_ID_IOS in
     * local.properties with your own AdMob console's real IDs before publishing.
     */
    internal object AdsConfig {
        const val INTERSTITIAL_UNIT_ID = "$admobInterstitialUnitId"
        const val REWARDED_UNIT_ID = "$admobRewardedUnitId"
        const val INTERSTITIAL_UNIT_ID_IOS = "$admobInterstitialUnitIdIos"
        const val REWARDED_UNIT_ID_IOS = "$admobRewardedUnitIdIos"
        const val DEBUG_GEO_EEA_TEST_DEVICE_ID = "$admobDebugGeoEeaTestDeviceId"
    }
    """.trimIndent()
)

File(generatedConfigDir, "GoogleAuthConfig.kt").writeText(
    """
    package com.konye.historiq.Core.Auth

    /** Generated at Gradle configuration time from local.properties — see GoogleSignIn.android.kt. */
    internal object GoogleAuthConfig {
        const val WEB_CLIENT_ID = "${localProperty("GOOGLE_WEB_CLIENT_ID")}"
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
                // 1.7.3 (a pre-iOS-stability release) and won't resolve for the iOS target at all.
                // There's no smaller "core" icon set exposed by the CMP Gradle plugin either
                // (confirmed by reading its own Dependencies source — materialIconsExtended is the
                // only icon accessor it has). The app only uses 4 distinct icons app-wide, bundled
                // instead as vector drawable resources (commonMain/composeResources/drawable/) per
                // JetBrains' own deprecation note ("migrate to Material Symbols / vector resources").
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
                // Cross-platform key-value storage — Android SharedPreferences-equivalent /
                // NSUserDefaults-equivalent, replacing this project's hand-rolled Context-based
                // SharedPreferences access in the four data stores.
                implementation("com.russhwolf:multiplatform-settings:1.3.0")
                // 0.7.1, not the latest: supabase-kt 3.2.4 strictly constrains kotlinx-datetime to
                // 0.7.1 (confirmed via dependencyInsight) — declaring that version directly instead
                // of an older one that Gradle would silently override anyway.
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.2")
                implementation("androidx.core:core-ktx:1.13.1")
                implementation("io.ktor:ktor-client-android:3.3.0")
                // Real interstitial/rewarded ads on Android (see Core/Ads/InterstitialAd.android.kt,
                // Core/Ads/RewardedAd.android.kt) — iOS ads are real too, via a Swift bridge to the
                // GoogleMobileAds SPM package (iosApp/iosApp/AdsBridge.swift), not this dependency.
                implementation("com.google.android.gms:play-services-ads:25.4.0")
                // EEA/UK ad-consent flow (Core/Ads/ConsentManager.android.kt) — not bundled
                // transitively by play-services-ads, a separate artifact.
                implementation("com.google.android.ump:user-messaging-platform:4.0.0")
                // Real "Continue with Google" on Android (Core/Auth/GoogleSignIn.android.kt) via
                // Credential Manager — iOS Google sign-in is real too, via a Swift bridge to the
                // GoogleSignIn SPM package (iosApp/iosApp/GoogleSignInBridge.swift), not this dependency.
                implementation("androidx.credentials:credentials:1.6.0")
                implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
                implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")
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

compose.resources {
    // Otherwise auto-derived from rootProject.name ("Historiq") + module name into an awkward
    // "historiq.composeapp.generated.resources" — this matches the rest of the app's package style.
    packageOfResClass = "com.konye.historiq.generated.resources"
}

// Play Console requires a signed .aab to upload a release. A real keystore already exists at
// composeApp/release.jks (gitignored, generated 2026-09-15) with RELEASE_STORE_FILE/
// RELEASE_STORE_PASSWORD/RELEASE_KEY_ALIAS/RELEASE_KEY_PASSWORD in local.properties (also
// gitignored, same as the Supabase keys above) — back both up somewhere durable, since losing
// either means never being able to publish an update to this app under the same listing again.
// This still stays conditionally inert (no signingConfig applied) so the project keeps building
// for anyone who clones it without that keystore.
val hasReleaseSigning = localProperty("RELEASE_STORE_FILE").isNotBlank()

android {
    namespace = "com.konye.historiq"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.konye.historiq"
        minSdk = 26
        targetSdk = 36
        versionCode = 17
        versionName = "0.17"
        manifestPlaceholders["admobApplicationId"] = admobAppId
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(localProperty("RELEASE_STORE_FILE"))
                storePassword = localProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
