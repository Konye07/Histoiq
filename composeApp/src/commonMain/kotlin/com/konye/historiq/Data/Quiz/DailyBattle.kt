package com.konye.historiq.Data.Quiz

import com.konye.historiq.Core.Network.retryNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * One question of the Daily Battle, handed to the screen of the game it comes from. The screen asks it as a round of that one
 * question, drawn and saved under [progressKey], and when it is answered calls [onFinished] with the question's score on the
 * battle's 0-1000 scale instead of showing results of its own (the battle shows one results screen for all six). [number] is
 * the question's place in the battle, counted from 1.
 */
class BattleLeg(val game: String, val number: Int, val onFinished: (score: Double) -> Unit) {
    val progressKey: String = GameKeys.battleKey(game)
    val total: Int get() = BattleLegs.size
}

/** The Daily Battle's questions, one per game, in the order they are asked: the Play menu's own. */
object BattleLegs {
    val games: List<String> = GameKeys.ALL
    val size: Int get() = games.size
}

/**
 * The game whose question the Daily Battle is showing at the moment, or null while it shows its overview or its results.
 * The bottom bar paints itself in that game's own color, as it does on the game's own screen.
 */
object BattleSession {
    private val state = MutableStateFlow<String?>(null)

    val activeGame: StateFlow<String?> = state.asStateFlow()

    fun show(game: String?) {
        state.value = game
    }
}

/**
 * Where a player stands in today's battle: the scores (0-1000) of the questions answered so far, in order, kept in
 * `quiz_progress` under [GameKeys.DAILY_BATTLE]. Each question is saved by its own game's screen under its battle key, so
 * one answered but not yet recorded here is found again, already answered, when its screen opens and reports it.
 */
object DailyBattleStore {
    suspend fun load(): List<Double> =
        retryNetwork { QuizProgressStore.load(GameKeys.DAILY_BATTLE) }?.getDoubleList("scores").orEmpty()

    suspend fun save(scores: List<Double>) {
        QuizProgressStore.save(GameKeys.DAILY_BATTLE, buildJsonObject {
            put("scores", JsonArray(scores.map { JsonPrimitive(it) }))
        })
    }
}
