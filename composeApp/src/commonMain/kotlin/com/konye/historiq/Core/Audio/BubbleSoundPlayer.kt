package com.konye.historiq.Core.Audio

/**
 * A short, procedurally synthesized "bubbling up" sound (a handful of quick, rising blips) for
 * the results-reveal animation — no bundled audio asset needed. Reuses the haptics-enabled
 * preference as the general "feedback effects" toggle, since there's no separate sound setting.
 */
expect object BubbleSoundPlayer {
    fun playBubbleRise()
}
