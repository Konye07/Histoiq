package com.konye.historiq.Core.Audio

import com.konye.historiq.Data.Settings.SettingsStore
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create
import kotlin.math.roundToInt

/** Kept alive for the duration of playback — an unretained AVAudioPlayer can stop mid-clip. */
@OptIn(ExperimentalForeignApi::class)
private var activePlayer: AVAudioPlayer? = null

actual object BubbleSoundPlayer {
    actual fun playBubbleRise() {
        if (!SettingsStore.hapticsEnabled()) return
        CoroutineScope(Dispatchers.Main).launch {
            try {
                playWav(buildWavBytes(buildBubbleRiseWaveform()))
            } catch (_: Exception) {
                // Best-effort feedback only; never crash the results screen over a sound effect.
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun playWav(wavBytes: ByteArray) {
        val nsData = wavBytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = wavBytes.size.toULong())
        }
        val player = AVAudioPlayer(data = nsData, error = null)
        activePlayer = player
        player.play()
    }

    /** Minimal 16-bit PCM mono WAV container — AVAudioPlayer reads the format straight from it. */
    private fun buildWavBytes(samples: FloatArray): ByteArray {
        val bytesPerSample = 2
        val dataSize = samples.size * bytesPerSample
        val byteRate = BUBBLE_SAMPLE_RATE * bytesPerSample
        val bytes = ByteArray(44 + dataSize)

        fun writeAscii(offset: Int, text: String) {
            text.forEachIndexed { i, c -> bytes[offset + i] = c.code.toByte() }
        }
        fun writeLE32(offset: Int, value: Int) {
            bytes[offset] = (value and 0xFF).toByte()
            bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
            bytes[offset + 2] = ((value shr 16) and 0xFF).toByte()
            bytes[offset + 3] = ((value shr 24) and 0xFF).toByte()
        }
        fun writeLE16(offset: Int, value: Int) {
            bytes[offset] = (value and 0xFF).toByte()
            bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
        }

        writeAscii(0, "RIFF")
        writeLE32(4, 36 + dataSize)
        writeAscii(8, "WAVE")
        writeAscii(12, "fmt ")
        writeLE32(16, 16)
        writeLE16(20, 1) // PCM
        writeLE16(22, 1) // mono
        writeLE32(24, BUBBLE_SAMPLE_RATE)
        writeLE32(28, byteRate)
        writeLE16(32, bytesPerSample) // block align
        writeLE16(34, 16) // bits per sample
        writeAscii(36, "data")
        writeLE32(40, dataSize)

        var offset = 44
        for (sample in samples) {
            val clamped = (sample * Short.MAX_VALUE).roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            writeLE16(offset, clamped)
            offset += 2
        }
        return bytes
    }
}
