package com.yourorg.app.Core.Platform

/**
 * Real handling (`UIApplication.sharedApplication.openURL`) arrives with the rest of the isolated
 * platform-specific concerns. A safe no-op for now: the Privacy Policy screen isn't reachable from
 * any iOS screen yet either, so nothing depends on this being real yet.
 */
actual fun openUrl(url: String) {}
