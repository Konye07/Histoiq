package com.konye.historiq.Data.Quiz

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private fun Double.toRadians(): Double = this * PI / 180.0

private fun Double.roundTo2(): Double = (this * 100.0).roundToInt() / 100.0

/** Formats a signed year (negative = BCE, per this app's data convention) as e.g. "1415 AD" / "480 BCE". */
fun formatEraYear(year: Int): String = if (year < 0) "${-year} BCE" else "$year AD"

/** Formats a whole number with a space as the thousands separator, e.g. 14000 -> "14 000". */
fun formatThousands(value: Long): String {
    val negative = value < 0
    val digits = abs(value).toString()
    val grouped = digits.reversed().chunked(3).joinToString(" ").reversed()
    return if (negative) "-$grouped" else grouped
}

/**
 * Linear falloff from 1000 down to 100 over a difficulty-scaled tolerance window,
 * floored at 100 beyond that window.
 */
fun yearGuessScore(guess: Int, correctYear: Int, difficulty: Int): Double {
    val distance = abs(guess - correctYear).toDouble()
    val tolerance = 30.0 + difficulty * 20.0
    val raw = 1000.0 - (distance / tolerance) * 900.0
    return raw.coerceIn(100.0, 1000.0).roundTo2()
}

/**
 * Multiplier is the ratio between the guess and the correct value: correct/guess when you
 * guess under it, guess/correct when you guess over it — always >= 1, exact on a perfect guess.
 */
fun casualtyMultiplier(guess: Long, correctCasualties: Long): Double {
    if (guess <= 0 || correctCasualties <= 0) return 1.0
    val ratio = max(guess, correctCasualties).toDouble() / min(guess, correctCasualties).toDouble()
    return ratio.roundTo2()
}

/**
 * Being in the wrong color family entirely (e.g. blue instead of red) is judged as its own,
 * dominant penalty rather than being averaged in with saturation/brightness — the old plain
 * weighted-linear-distance version let a completely wrong hue still score 7-8/10 as long as
 * saturation/brightness happened to match, since hue was only half the total weight and moved
 * linearly. Hue error is now: (a) the large majority of the weight, and (b) squared once past a
 * small tolerance window, so a near-miss on hue is barely penalized but a different-color guess
 * collapses toward the 1.0 floor regardless of how well saturation/brightness were matched.
 */
fun colorMatchScore(
    guessHue: Int,
    guessSaturation: Int,
    guessBrightness: Int,
    correctHue: Int,
    correctSaturation: Int,
    correctBrightness: Int
): Double {
    val hueDegrees = abs(guessHue - correctHue).let { min(it, 360 - it) }.toDouble()
    val satDiff = abs(guessSaturation - correctSaturation) / 100.0
    val brightDiff = abs(guessBrightness - correctBrightness) / 100.0

    val hueTolerance = 40.0
    val huePenalty = (hueDegrees / hueTolerance).coerceAtMost(1.0).let { it * it }
    val fineDiff = satDiff * 0.3 + brightDiff * 0.3 + (hueDegrees / 180.0) * 0.4
    val distance = (huePenalty * 0.8 + fineDiff * 0.2).coerceIn(0.0, 1.0)
    return (10.0 - distance * 9.0).coerceIn(1.0, 10.0).roundTo2()
}

/** Great-circle distance between two lat/lng points, in kilometers. */
fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = (lat2 - lat1).toRadians()
    val dLng = (lng2 - lng1).toRadians()
    val a = sin(dLat / 2).pow(2) +
        cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLng / 2).pow(2)
    val c = 2.0 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}

/** Linear falloff from 1000 to 100 over a 3000km tolerance window, floored at 100 beyond that. */
fun mapPinpointScore(distanceKm: Double): Double {
    val tolerance = 3000.0
    val raw = 1000.0 - (distanceKm / tolerance) * 900.0
    return raw.coerceIn(100.0, 1000.0).roundTo2()
}
