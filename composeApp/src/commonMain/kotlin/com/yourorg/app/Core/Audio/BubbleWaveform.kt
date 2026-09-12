package com.yourorg.app.Core.Audio

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
