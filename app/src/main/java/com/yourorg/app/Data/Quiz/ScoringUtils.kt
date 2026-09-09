package com.yourorg.app.Data.Quiz

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

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
 * Linear falloff from 10 down to 1 over a difficulty-scaled tolerance window,
 * floored at 1 beyond that window.
 */
fun yearGuessScore(guess: Int, correctYear: Int, difficulty: Int): Double {
    val distance = abs(guess - correctYear).toDouble()
    val tolerance = 30.0 + difficulty * 20.0
    val raw = 10.0 - (distance / tolerance) * 9.0
    return raw.coerceIn(1.0, 10.0).roundTo2()
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
 * Weighted distance across hue (circular), saturation and brightness, mapped to a 1-10 score.
 * Hue is weighted higher since it dominates perceived color difference.
 */
fun colorMatchScore(
    guessHue: Int,
    guessSaturation: Int,
    guessBrightness: Int,
    correctHue: Int,
    correctSaturation: Int,
    correctBrightness: Int
): Double {
    val hueDiff = abs(guessHue - correctHue).let { min(it, 360 - it) } / 180.0
    val satDiff = abs(guessSaturation - correctSaturation) / 100.0
    val brightDiff = abs(guessBrightness - correctBrightness) / 100.0
    val distance = hueDiff * 0.5 + satDiff * 0.25 + brightDiff * 0.25
    return (10.0 - distance * 9.0).coerceIn(1.0, 10.0).roundTo2()
}

/** Great-circle distance between two lat/lng points, in kilometers. */
fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    val c = 2.0 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}

/** Linear falloff from 10 to 1 over a 3000km tolerance window, floored at 1 beyond that. */
fun mapPinpointScore(distanceKm: Double): Double {
    val tolerance = 3000.0
    val raw = 10.0 - (distanceKm / tolerance) * 9.0
    return raw.coerceIn(1.0, 10.0).roundTo2()
}
