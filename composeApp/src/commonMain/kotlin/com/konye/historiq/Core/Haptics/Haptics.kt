package com.konye.historiq.Core.Haptics

/**
 * Distinct vibration patterns for different feedback moments. All check the haptics-enabled
 * preference internally, so callers never need to guard a call themselves.
 */
expect object Haptics {
    /** Very light tap — opening a game, or switching Play/Friends/Profile/Settings. */
    fun tick()

    /** Right answer — Cosmographer / Chronicler. */
    fun correct()

    /** Wrong answer / game over — Cosmographer / Chronicler. */
    fun wrong()

    /** Perfect score — Annalist, Herald, Officer-of-Arms, Chancellor. */
    fun perfect()

    /** Escalating buzz synced to the results screen's "score bubbling up" reveal animation. */
    fun scoreReveal()
}
