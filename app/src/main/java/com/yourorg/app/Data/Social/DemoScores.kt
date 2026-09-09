package com.yourorg.app.Data.Social

import com.yourorg.app.Data.Quiz.GameKeys
import kotlin.random.Random

/**
 * Deterministic pseudo-random score for a demo friend on one game/day — stable across app
 * restarts (seeded from the code+game+date), standing in for what a real backend would return.
 * Returns null if the demo friend "didn't play" that day.
 */
fun demoScoreFor(friendCode: String, quizKey: String, dateKey: String): Double? {
    val random = Random("$friendCode|$quizKey|$dateKey".hashCode().toLong())
    if (random.nextInt(100) < 15) return null

    return when (quizKey) {
        GameKeys.FLAG_COLOR_MATCH ->
            random.nextInt(150, 501) / 10.0
        GameKeys.YEAR_GUESS, GameKeys.BLIND_MAP_BATTLE ->
            random.nextInt(1500, 5001).toDouble()
        GameKeys.CASUALTIES_MULTIPLIER ->
            random.nextInt(100, 351) / 100.0
        GameKeys.TERRITORY_COMPARE ->
            if (random.nextBoolean()) 10.0 else 0.0
        GameKeys.TRUE_FALSE ->
            random.nextInt(0, 11).toDouble()
        else -> 0.0
    }
}
