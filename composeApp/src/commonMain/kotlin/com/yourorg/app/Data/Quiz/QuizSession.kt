package com.yourorg.app.Data.Quiz

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

/** [resumedState] is null on a fresh start; non-null (and already parsed) when resuming today's quiz. */
data class QuizResume<T>(val questions: List<T>, val resumedState: JsonObject?)

/**
 * The "which ids today, resume-or-fresh" shell every fixed-question-count game repeats identically
 * — only [YearGuess], [CasualtiesMultiplier], [BlindMapBattle] and [TrueFalse] use this.
 * [TerritoryCompare] is a genuinely different (survival-queue) state machine, not a shape variant
 * of this one. [FlagColorMatch] almost fits, but a fresh start needs to generate and persist
 * randomized per-question target colors (an async per-flag image lookup) before any answer exists
 * — a third category of state beyond "ids" and "answers" that this shell doesn't model — so both
 * are intentionally left with their own bespoke resume/persist logic instead of being forced
 * through here.
 *
 * Each game still extracts and persists its own answer fields from/into [QuizResume.resumedState] —
 * those differ in real shape per game (YearGuess: guesses+scores; TrueFalse: answers+failed) —
 * this only removes the identical id-resolution bookkeeping.
 */
suspend fun <T> resumeOrStartQuiz(
    progressKey: String,
    questionsPerRound: Int,
    questionId: (T) -> String,
    loadQuestions: suspend (ids: List<String>) -> List<T>
): QuizResume<T> {
    val stored = QuizProgressStore.load(progressKey)?.takeIf { "ids" in it }
    val ids = if (stored != null) {
        stored.getStringList("ids")
    } else {
        DailyQuizRepository.getOrCreate(progressKey, DailyClock.todayKey(), questionsPerRound)
    }

    val rows = loadQuestions(ids)
    val questions = ids.mapNotNull { id -> rows.find { questionId(it) == id } }

    if (stored == null) {
        QuizProgressStore.save(progressKey, buildJsonObject {
            put("ids", JsonArray(ids.map { JsonPrimitive(it) }))
        })
    }

    return QuizResume(questions, stored)
}

/** Reads a stored string array field (e.g. "ids"), or an empty list if the field is absent. */
fun JsonObject.getStringList(field: String): List<String> =
    (this[field] as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList()

/** Reads a stored int array field, or an empty list if the field is absent (a fresh, answerless start). */
fun JsonObject.getIntList(field: String): List<Int> =
    (this[field] as? JsonArray)?.map { it.jsonPrimitive.int } ?: emptyList()

/** Reads a stored long array field, or an empty list if the field is absent. */
fun JsonObject.getLongList(field: String): List<Long> =
    (this[field] as? JsonArray)?.map { it.jsonPrimitive.long } ?: emptyList()

/** Reads a stored double array field, or an empty list if the field is absent. */
fun JsonObject.getDoubleList(field: String): List<Double> =
    (this[field] as? JsonArray)?.map { it.jsonPrimitive.double } ?: emptyList()

/** Reads a stored boolean array field, or an empty list if the field is absent. */
fun JsonObject.getBooleanList(field: String): List<Boolean> =
    (this[field] as? JsonArray)?.map { it.jsonPrimitive.boolean } ?: emptyList()
