package com.konye.historiq.Core.Audio

/**
 * A short, procedurally synthesized "bubbling up" sound (a handful of quick, rising blips) for
 * the results-reveal animation — no bundled audio asset needed. Reuses the haptics-enabled
 * preference as the general "feedback effects" toggle, since there's no separate sound setting.
 */
expect object BubbleSoundPlayer {
    fun playBubbleRise()

    /** A good/correct per-question result. */
    fun playCorrectChirp()

    /** A weak/wrong per-question result. */
    fun playWrongTone()

    /** Confirms a guess was submitted, ahead of it being scored. */
    fun playSubmitTap()
}
