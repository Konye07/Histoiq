package com.yourorg.app.Data.Quiz

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

/**
 * Synthetic "how did today's other players do" data, in the same spirit as [demoScoreFor] in
 * Data/Social/DemoScores.kt — deterministically seeded so it's stable across recompositions and
 * app restarts, standing in for real aggregate data until a backend exists. Used to show a
 * results-screen ranking and per-question guess distributions.
 */
object DailyDistribution {
    private const val FINAL_SCORE_SAMPLES = 200
    private const val GUESS_SAMPLES = 120

    /** A roughly bell-shaped value in [-1, 1] (average of three uniform samples). */
    private fun bellUnit(random: Random): Double {
        val a = random.nextDouble(-1.0, 1.0)
        val b = random.nextDouble(-1.0, 1.0)
        val c = random.nextDouble(-1.0, 1.0)
        return (a + b + c) / 3.0
    }

    /** Synthetic final-score distribution for one game/day, on that game's own scoring scale. */
    fun finalScores(gameKey: String, dateKey: String): List<Double> {
        val random = Random("finalscore|$gameKey|$dateKey".hashCode().toLong())
        return List(FINAL_SCORE_SAMPLES) {
            when (gameKey) {
                GameKeys.FLAG_COLOR_MATCH ->
                    random.nextInt(150, 501) / 10.0
                GameKeys.YEAR_GUESS, GameKeys.BLIND_MAP_BATTLE ->
                    random.nextInt(1500, 5001).toDouble()
                GameKeys.CASUALTIES_MULTIPLIER ->
                    random.nextInt(100, 351) / 100.0
                GameKeys.TERRITORY_COMPARE, GameKeys.TRUE_FALSE ->
                    random.nextInt(0, 11).toDouble()
                else -> 0.0
            }
        }
    }

    /** Percentile (0-100): how many of today's synthetic players this result beats. */
    fun percentile(value: Double, samples: List<Double>, higherIsBetter: Boolean): Int {
        if (samples.isEmpty()) return 50
        val beaten = samples.count { if (higherIsBetter) it < value else it > value }
        return ((beaten * 100.0) / samples.size).roundToInt().coerceIn(0, 100)
    }

    /** Synthetic total number of players today for one game — for display as "rank X of Y". */
    fun totalPlayers(gameKey: String, dateKey: String): Int {
        val random = Random("totalplayers|$gameKey|$dateKey".hashCode().toLong())
        return random.nextInt(6000, 22000)
    }

    /** Rank (1 = best) among [totalPlayers], derived from a beaten-fraction percentile. */
    fun rank(percentile: Int, totalPlayers: Int): Int {
        val fractionBeaten = percentile / 100.0
        return ((1.0 - fractionBeaten) * totalPlayers).roundToInt().coerceIn(1, totalPlayers)
    }

    /** Convenience: (rank, totalPlayers) for one game/day given the player's value and a comparison distribution. */
    fun rankAmongPlayers(gameKey: String, dateKey: String, value: Double, samples: List<Double>, higherIsBetter: Boolean): Pair<Int, Int> {
        val total = totalPlayers(gameKey, dateKey)
        val pct = percentile(value, samples, higherIsBetter)
        return rank(pct, total) to total
    }

    /** Synthetic year guesses other players made on this exact question, clustered around the truth. */
    fun yearGuesses(dateKey: String, questionId: String, correctYear: Int, difficulty: Int): List<Int> {
        val random = Random("yearguess|$dateKey|$questionId".hashCode().toLong())
        val tolerance = 30.0 + difficulty * 20.0
        return List(GUESS_SAMPLES) {
            val spread = tolerance * (0.5 + random.nextDouble() * 3.0)
            (correctYear + bellUnit(random) * spread).roundToInt()
        }
    }

    /** Synthetic casualty guesses other players made on this exact question, log-scale around the truth. */
    fun casualtyGuesses(dateKey: String, questionId: String, correctCasualties: Long): List<Long> {
        val random = Random("casguess|$dateKey|$questionId".hashCode().toLong())
        return List(GUESS_SAMPLES) {
            val factor = exp(bellUnit(random) * 1.1)
            (correctCasualties * factor).roundToLong().coerceAtLeast(0)
        }
    }

    /** Synthetic final casualty multipliers (product of 5 per-question multipliers) for today. */
    fun casualtyMultipliers(dateKey: String): List<Double> {
        val random = Random("casmult|$dateKey".hashCode().toLong())
        return List(FINAL_SCORE_SAMPLES) {
            (1..5).fold(1.0) { acc, _ -> acc * (1.0 + abs(bellUnit(random)) * 1.2) }
        }
    }
}
