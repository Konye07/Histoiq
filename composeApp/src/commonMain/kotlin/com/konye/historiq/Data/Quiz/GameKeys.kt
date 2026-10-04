package com.konye.historiq.Data.Quiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.konye.historiq.Core.Formatting.formatFixed
import com.konye.historiq.Core.Localization.StringsEras
import com.konye.historiq.Core.Localization.StringsGamekeys
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Core.Theme.EraThemes

/**
 * Central registry of the six quizzes' progress-store keys, so Friends/Profile/Stats agree with each game screen.
 * Role/title/color/score formatting also accept an era mode key (see [modeKey]) and resolve it to its base game.
 */
object GameKeys {
    const val YEAR_GUESS = "year_guess"
    const val CASUALTIES_MULTIPLIER = "casualties_multiplier"
    const val FLAG_COLOR_MATCH = "flag_color_match"
    const val TERRITORY_COMPARE = "territory_compare"
    const val TRUE_FALSE = "true_false"
    const val BLIND_MAP_BATTLE = "blind_map_battle"

    val ALL = listOf(YEAR_GUESS, CASUALTIES_MULTIPLIER, FLAG_COLOR_MATCH, TERRITORY_COMPARE, TRUE_FALSE, BLIND_MAP_BATTLE)

    /** The games whose questions each carry a year, so a [QuizEra] can filter them — the other three have no per-question date. */
    val ERA_FILTERABLE = setOf(YEAR_GUESS, CASUALTIES_MULTIPLIER, BLIND_MAP_BATTLE)

    private const val ERA_SEPARATOR = ':'

    /**
     * The key one game's rounds, scores, ranks and stats are stored under: the plain game key for
     * All-time ([era] null) or for a game with no era filter, "<game>:<era>" otherwise. Every table keyed
     * by game treats an era mode as a game of its own, which is what gives each era its own daily round,
     * streak and stats; the server splits the suffix off again to pick the question bank and the era.
     */
    fun modeKey(gameKey: String, era: QuizEra?): String =
        if (era != null && gameKey in ERA_FILTERABLE) "$gameKey$ERA_SEPARATOR${era.key}" else gameKey

    fun baseKey(modeKey: String): String = modeKey.substringBefore(ERA_SEPARATOR)

    fun eraOf(modeKey: String): QuizEra? = QuizEra.fromKey(modeKey.substringAfter(ERA_SEPARATOR, ""))

    @Composable
    fun roleName(key: String): String = when (baseKey(key)) {
        YEAR_GUESS -> StringsGamekeys.role_annalist
        CASUALTIES_MULTIPLIER -> StringsGamekeys.role_herald
        FLAG_COLOR_MATCH -> StringsGamekeys.role_officer_of_arms
        TERRITORY_COMPARE -> StringsGamekeys.role_cosmographer
        TRUE_FALSE -> StringsGamekeys.role_chronicler
        BLIND_MAP_BATTLE -> StringsGamekeys.role_chancellor
        else -> key
    }

    /** The game's title, with the era in parentheses for an era mode key — what Friends, Profile and the share text show. */
    @Composable
    fun title(key: String): String {
        val base = when (baseKey(key)) {
            YEAR_GUESS -> StringsGamekeys.title_year_guess
            CASUALTIES_MULTIPLIER -> StringsGamekeys.title_casualties_multiplier
            FLAG_COLOR_MATCH -> StringsGamekeys.title_flag_color_match
            TERRITORY_COMPARE -> StringsGamekeys.title_territory_compare
            TRUE_FALSE -> StringsGamekeys.title_true_false
            BLIND_MAP_BATTLE -> StringsGamekeys.title_blind_map_battle
            else -> key
        }
        val era = eraOf(key) ?: return base
        return "$base (${StringsEras.label(era)})"
    }

    /** The game's signature color, or the era's own color for it when [key] is an era mode key. */
    fun color(key: String): Color {
        val era = eraOf(key)
        if (era != null) return EraThemes.palette(era).forGame(baseKey(key))
        return when (baseKey(key)) {
            YEAR_GUESS -> AppColors.SlateBlue
            CASUALTIES_MULTIPLIER -> AppColors.Maroon
            FLAG_COLOR_MATCH -> AppColors.OliveDark
            TERRITORY_COMPARE -> AppColors.ForestDark
            TRUE_FALSE -> AppColors.BrownDark
            BLIND_MAP_BATTLE -> AppColors.EspressoDark
            else -> AppColors.Tan
        }
    }

    fun formatScore(key: String, score: Double): String = when (baseKey(key)) {
        CASUALTIES_MULTIPLIER -> "x" + formatFixed(score, 2)
        else -> formatFixed(score, 2)
    }
}
