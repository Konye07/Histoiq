package com.yourorg.app.Core.Audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.yourorg.app.Data.Settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

actual object BubbleSoundPlayer {
    private const val SAMPLE_RATE = 44100
    private val blipFrequenciesHz = listOf(500.0, 700.0, 950.0, 1250.0, 1600.0)
    private const val BLIP_DURATION_MS = 110
    private const val GAP_MS = 25

    actual fun playBubbleRise() {
        if (!SettingsStore.hapticsEnabled()) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                playSamples(buildWaveform())
            } catch (_: Exception) {
                // Best-effort feedback only; never crash the results screen over a sound effect.
            }
        }
    }

    private fun buildWaveform(): ShortArray {
        val blipSamples = SAMPLE_RATE * BLIP_DURATION_MS / 1000
        val gapSamples = SAMPLE_RATE * GAP_MS / 1000
        val buffer = ShortArray(blipFrequenciesHz.size * (blipSamples + gapSamples))
        var idx = 0
        for (freq in blipFrequenciesHz) {
            for (i in 0 until blipSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val instantFreq = freq * (1.0 + 0.6 * i.toDouble() / blipSamples)
                val phase = 2.0 * PI * instantFreq * t
                val envelope = exp(-6.0 * i.toDouble() / blipSamples)
                buffer[idx++] = (sin(phase) * envelope * Short.MAX_VALUE * 0.5).toInt().toShort()
            }
            idx += gapSamples
        }
        return buffer
    }

    private fun playSamples(samples: ShortArray) {
        val audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            samples.size * 2,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        audioTrack.write(samples, 0, samples.size)
        audioTrack.setNotificationMarkerPosition(samples.size)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) {
                track?.release()
            }
            override fun onPeriodicNotification(track: AudioTrack?) = Unit
        })
        audioTrack.play()
    }
}
