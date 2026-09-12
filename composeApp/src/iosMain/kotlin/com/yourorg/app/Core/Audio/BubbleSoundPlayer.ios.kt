package com.yourorg.app.Core.Audio

/**
 * Real playback (AVAudioPlayer, synthesizing the same waveform or a bundled equivalent) arrives
 * with the rest of the isolated platform-specific concerns (haptics, share, notifications) — a
 * safe no-op for now, not a crash risk.
 */
actual object BubbleSoundPlayer {
    actual fun playBubbleRise() {}
}
