pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "YourApp"
include(":app")
// KMP migration in progress (see CLAUDE.md / the migration plan): composeApp is the new
// commonMain/androidMain/iosMain module. `app` stays included as a rollback reference until
// Android parity is fully confirmed, then gets removed.
include(":composeApp")
