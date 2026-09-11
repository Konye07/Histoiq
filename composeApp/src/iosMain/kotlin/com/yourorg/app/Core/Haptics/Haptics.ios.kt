package com.yourorg.app.Core.Haptics

/**
 * Real haptic feedback (UIImpactFeedbackGenerator / UINotificationFeedbackGenerator) arrives with
 * the rest of the isolated platform-specific concerns (share, audio, notifications) — safe no-ops
 * for now, not a crash risk, just silent on iOS until that pass.
 */
actual object Haptics {
    actual fun tick() {}
    actual fun correct() {}
    actual fun wrong() {}
    actual fun perfect() {}
    actual fun scoreReveal() {}
}
