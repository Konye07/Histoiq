package com.konye.historiq.Core.Audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

internal const val BUBBLE_SAMPLE_RATE = 44100
private val blipFrequenciesHz = listOf(500.0, 700.0, 950.0, 1250.0, 1600.0)
private const val BLIP_DURATION_MS = 110
private const val GAP_MS = 25

/** A rising sequence of short pitched blips, normalized to [-1, 1] Float samples, mono. */
internal fun buildBubbleRiseWaveform(): FloatArray {
    val blipSamples = BUBBLE_SAMPLE_RATE * BLIP_DURATION_MS / 1000
    val gapSamples = BUBBLE_SAMPLE_RATE * GAP_MS / 1000
    val buffer = FloatArray(blipFrequenciesHz.size * (blipSamples + gapSamples))
    var idx = 0
    for (freq in blipFrequenciesHz) {
        for (i in 0 until blipSamples) {
            val t = i.toDouble() / BUBBLE_SAMPLE_RATE
            val instantFreq = freq * (1.0 + 0.6 * i.toDouble() / blipSamples)
            val phase = 2.0 * PI * instantFreq * t
            val envelope = exp(-6.0 * i.toDouble() / blipSamples)
            buffer[idx++] = (sin(phase) * envelope * 0.5).toFloat()
        }
        idx += gapSamples
    }
    return buffer
}

private const val CHIRP_DURATION_MS = 90

/** A single bright upward chirp for a correct/good per-question result — quicker and higher than
 * the full bubble-rise sequence, since this can fire multiple times per round, not just once at
 * the very end. */
internal fun buildCorrectChirpWaveform(): FloatArray {
    val samples = BUBBLE_SAMPLE_RATE * CHIRP_DURATION_MS / 1000
    val buffer = FloatArray(samples)
    val startFreq = 900.0
    val endFreq = 1400.0
    for (i in 0 until samples) {
        val t = i.toDouble() / BUBBLE_SAMPLE_RATE
        val progress = i.toDouble() / samples
        val instantFreq = startFreq + (endFreq - startFreq) * progress
        val phase = 2.0 * PI * instantFreq * t
        val envelope = exp(-5.0 * progress)
        buffer[i] = (sin(phase) * envelope * 0.5).toFloat()
    }
    return buffer
}

private const val WRONG_TONE_DURATION_MS = 160

/** A short, low, gently descending tone for a wrong/weak per-question result — deliberately dull
 * and soft rather than harsh, so a bad guess doesn't feel punishing. */
internal fun buildWrongToneWaveform(): FloatArray {
    val samples = BUBBLE_SAMPLE_RATE * WRONG_TONE_DURATION_MS / 1000
    val buffer = FloatArray(samples)
    val startFreq = 260.0
    val endFreq = 180.0
    for (i in 0 until samples) {
        val t = i.toDouble() / BUBBLE_SAMPLE_RATE
        val progress = i.toDouble() / samples
        val instantFreq = startFreq + (endFreq - startFreq) * progress
        val phase = 2.0 * PI * instantFreq * t
        val envelope = exp(-4.0 * progress)
        buffer[i] = (sin(phase) * envelope * 0.45).toFloat()
    }
    return buffer
}

private const val SUBMIT_TAP_DURATION_MS = 35

/** A very brief, soft tap for confirming a submitted guess — a barely-there acknowledgment, not a
 * reward cue (that's what the chirp/tone above are for once the guess is scored). */
internal fun buildSubmitTapWaveform(): FloatArray {
    val samples = BUBBLE_SAMPLE_RATE * SUBMIT_TAP_DURATION_MS / 1000
    val buffer = FloatArray(samples)
    val freq = 600.0
    for (i in 0 until samples) {
        val t = i.toDouble() / BUBBLE_SAMPLE_RATE
        val progress = i.toDouble() / samples
        val phase = 2.0 * PI * freq * t
        val envelope = exp(-10.0 * progress)
        buffer[i] = (sin(phase) * envelope * 0.35).toFloat()
    }
    return buffer
}
