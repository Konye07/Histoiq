package com.konye.historiq.Data.Quiz

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private fun Double.toRadians(): Double = this * PI / 180.0

private fun Double.roundTo2(): Double = (this * 100.0).roundToInt() / 100.0

/** Formats a whole number with a space as the thousands separator, e.g. 14000 -> "14 000". */
fun formatThousands(value: Long): String {
    val negative = value < 0
    val digits = abs(value).toString()
    val grouped = digits.reversed().chunked(3).joinToString(" ").reversed()
    return if (negative) "-$grouped" else grouped
}

/** A year guess this many years off (or more) scores the minimum. */
const val YEAR_GUESS_WINDOW_YEARS = 200.0

/** Score at or above which a year guess counts as a good one: within 50 years. */
const val YEAR_GUESS_GOOD_SCORE = 775.0

/** Linear falloff from 1000 down to 100 over [YEAR_GUESS_WINDOW_YEARS], floored at 100 beyond that. */
fun yearGuessScore(guess: Int, correctYear: Int): Double {
    val distance = abs(guess.toLong() - correctYear.toLong()).toDouble()
    val raw = 1000.0 - (distance / YEAR_GUESS_WINDOW_YEARS) * 900.0
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
 * A round's final multiplier is the *geometric* mean of its per-question multipliers, not an
 * arithmetic average or their raw product — the product alone compounds with question count
 * (five so-so 1.2x guesses would multiply out to ~2.49x, not stay near 1.2x), and an arithmetic
 * average would let one bad question be diluted by good ones the way an averaged colorMatchScore
 * used to. The Nth root undoes exactly that compounding: every question pulls equal weight
 * regardless of how many there are.
 */
fun geometricMeanMultiplier(multipliers: List<Double>): Double {
    if (multipliers.isEmpty()) return 1.0
    val product = multipliers.fold(1.0) { acc, m -> acc * m }
    return product.pow(1.0 / multipliers.size).roundTo2()
}

/**
 * Each axis's accuracy (1.0 = exact, 0.0 = maximally wrong) is *multiplied* together rather than
 * averaged. An earlier averaged version let a guess "pay for" a badly-wrong axis with a good one —
 * e.g. nailing hue but dragging brightness to near zero (the swatch reading as black instead of the
 * target color) still landed 8-9/10, since hue's near-zero distance dragged the 3-way average down
 * with it. Multiplying instead means every axis has to actually be close for the product to stay
 * high: three so-so guesses (e.g. 0.7/0.88/0.65 accuracy) compound to a ~40%-of-max score, not the
 * ~77% a plain average would give, and a maximally-wrong axis (accuracy 0) always floors the whole
 * question at the minimum regardless of the other two.
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

    val hueAccuracy = 1.0 - hueDiff
    val satAccuracy = 1.0 - satDiff
    val brightAccuracy = 1.0 - brightDiff

    val overallAccuracy = hueAccuracy * satAccuracy * brightAccuracy
    return (1.0 + overallAccuracy * 9.0).coerceIn(1.0, 10.0).roundTo2()
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

/*
 * The Daily Battle asks one question from each of six games and adds the six answers into one score, so each answer is
 * first put on the same 0-1000 scale (every game keeps its own scoring inside its own screen; only the answered question
 * is converted here). A perfect answer is 1000 in all six, a wrong one is 0 or the game's own minimum stretched to 0, and
 * the battle's total is the sum: 0-6000. The functions take a list because a game's own round is a list of questions; the
 * battle's round holds exactly one.
 */

/** Points one question can add to the Daily Battle. */
const val BATTLE_GAME_MAX = 1000.0

/** The battle holds one question per game, so its best possible score is six times a question's. */
val BATTLE_MAX_SCORE: Double get() = GameKeys.ALL.size * BATTLE_GAME_MAX

/** The average of per-question scores that run [lowest]-[highest] (Annalist and Chancellor 100-1000, Officer-of-Arms 1-10), stretched to 0-1000. */
fun battleAverageScore(questionScores: List<Double>, lowest: Double, highest: Double): Double {
    if (questionScores.isEmpty()) return 0.0
    return ((questionScores.average() - lowest) / (highest - lowest) * BATTLE_GAME_MAX).coerceIn(0.0, BATTLE_GAME_MAX).roundTo2()
}

/**
 * Herald's score is its multiplier (1 is exact, higher is worse, with no upper limit; a round of several questions is
 * scored by the geometric mean of theirs). A hundredfold miss or worse is 0, exact is 1000, and each doubling of the
 * multiplier costs the same: 3x off is about 760, 10x about 500.
 */
fun battleCasualtiesScore(multipliers: List<Double>): Double {
    if (multipliers.isEmpty()) return 0.0
    val geometricMean = geometricMeanMultiplier(multipliers)
    return (BATTLE_GAME_MAX * (1.0 - log10(geometricMean) / 2.0)).coerceIn(0.0, BATTLE_GAME_MAX).roundTo2()
}

/** Cosmographer and Chronicler: the share of the [rounds] answered right (the battle's one question is all or nothing). */
fun battleSurvivalScore(correct: Int, rounds: Int): Double =
    (correct.toDouble() / rounds * BATTLE_GAME_MAX).coerceIn(0.0, BATTLE_GAME_MAX).roundTo2()
