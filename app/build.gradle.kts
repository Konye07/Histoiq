import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

/** Loads SUPABASE_URL / SUPABASE_ANON_KEY from local.properties (gitignored) — never commit real values. */
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun localProperty(key: String): String = localProperties.getProperty(key, "")

android {
    namespace = "com.yourorg.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yourorg.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"

        buildConfigField("String", "SUPABASE_URL", "\"${localProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperty("SUPABASE_ANON_KEY")}\"")
    }

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

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("com.google.dagger:hilt-android:2.58")
    kapt("com.google.dagger:hilt-android-compiler:2.58")

    // postgrest-kt transitively pulls in auth-kt-android (for automatic session-token attachment)
    // regardless of whether we depend on auth-kt ourselves, which drags in androidx.browser 1.10.0
    // for its OAuth Custom Tabs flow — a dependency needing AGP 8.9.1+ and compileSdk 36 that we
    // don't need yet since nothing here calls install(Auth) or any OAuth API. Excluded below.
    // Pinned to 3.2.4 (not latest) because that's the newest supabase-kt release still built with
    // Kotlin 2.2.20 — anything newer moved to Kotlin 2.4.0, whose metadata format no released
    // Hilt/Dagger version can parse yet (google/dagger#5190).
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt") {
        exclude(group = "androidx.browser", module = "browser")
    }
    implementation("io.ktor:ktor-client-android:3.3.0")
}
