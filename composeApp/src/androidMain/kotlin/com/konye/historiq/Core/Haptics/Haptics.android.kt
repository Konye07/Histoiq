package com.konye.historiq.Core.Haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.konye.historiq.Core.Platform.AndroidPlatformContext
import com.konye.historiq.Data.Settings.SettingsStore

actual object Haptics {
    actual fun tick() = vibrate(longArrayOf(0, 12), intArrayOf(0, 50))
    actual fun correct() = vibrate(longArrayOf(0, 35, 60, 35), intArrayOf(0, 150, 0, 150))
    actual fun wrong() = vibrate(longArrayOf(0, 220), intArrayOf(0, 200))
    actual fun perfect() = vibrate(longArrayOf(0, 30, 50, 30, 50, 70), intArrayOf(0, 160, 0, 160, 0, 255))
    actual fun scoreReveal() = vibrate(
        longArrayOf(0, 80, 40, 80, 40, 80, 40, 100, 40, 150),
        intArrayOf(0, 40, 0, 70, 0, 110, 0, 160, 0, 220)
    )

    private fun vibrate(timings: LongArray, amplitudes: IntArray) {
        if (!SettingsStore.hapticsEnabled()) return
        val vibrator = vibratorFor()
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    private fun vibratorFor(): Vibrator {
        val context = AndroidPlatformContext.app
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
