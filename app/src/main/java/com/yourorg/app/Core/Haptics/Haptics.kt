package com.yourorg.app.Core.Haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.yourorg.app.Data.Settings.SettingsStore

/**
 * Distinct vibration patterns for different feedback moments. All checks the haptics-enabled
 * preference internally, so callers never need to guard a call themselves.
 */
object Haptics {

    /** Very light tap — opening a game, or switching Play/Friends/Profile/Settings. */
    fun tick(context: Context) = vibrate(context, longArrayOf(0, 12), intArrayOf(0, 50))

    /** Right answer — Cosmographer / Chronicler. */
    fun correct(context: Context) = vibrate(context, longArrayOf(0, 35, 60, 35), intArrayOf(0, 150, 0, 150))

    /** Wrong answer / game over — Cosmographer / Chronicler. */
    fun wrong(context: Context) = vibrate(context, longArrayOf(0, 220), intArrayOf(0, 200))

    /** Perfect score — Annalist, Herald, Officer-of-Arms, Chancellor. */
    fun perfect(context: Context) =
        vibrate(context, longArrayOf(0, 30, 50, 30, 50, 70), intArrayOf(0, 160, 0, 160, 0, 255))

    /** Escalating buzz synced to the results screen's "score bubbling up" reveal animation. */
    fun scoreReveal(context: Context) = vibrate(
        context,
        longArrayOf(0, 80, 40, 80, 40, 80, 40, 100, 40, 150),
        intArrayOf(0, 40, 0, 70, 0, 110, 0, 160, 0, 220)
    )

    private fun vibrate(context: Context, timings: LongArray, amplitudes: IntArray) {
        if (!SettingsStore.hapticsEnabled(context)) return
        val vibrator = vibratorFor(context)
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    private fun vibratorFor(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
