package com.yourorg.app.Data.Quiz

import androidx.compose.ui.graphics.Color
import com.yourorg.app.Core.Theme.AppColors

/** Central registry of the six quizzes' progress-store keys, so Friends/Profile/Stats agree with each game screen. */
object GameKeys {
    const val YEAR_GUESS = "year_guess"
    const val CASUALTIES_MULTIPLIER = "casualties_multiplier"
    const val FLAG_COLOR_MATCH = "flag_color_match"
    const val TERRITORY_COMPARE = "territory_compare"
    const val TRUE_FALSE = "true_false"
    const val BLIND_MAP_BATTLE = "blind_map_battle"

    val ALL = listOf(YEAR_GUESS, CASUALTIES_MULTIPLIER, FLAG_COLOR_MATCH, TERRITORY_COMPARE, TRUE_FALSE, BLIND_MAP_BATTLE)

    fun roleName(key: String): String = when (key) {
        YEAR_GUESS -> "Annalist"
        CASUALTIES_MULTIPLIER -> "Herald"
        FLAG_COLOR_MATCH -> "Officer-of-Arms"
        TERRITORY_COMPARE -> "Cosmographer"
        TRUE_FALSE -> "Chronicler"
        BLIND_MAP_BATTLE -> "Chancellor"
        else -> key
    }

    fun title(key: String): String = when (key) {
        YEAR_GUESS -> "Guess the Year"
        CASUALTIES_MULTIPLIER -> "Casualty Count"
        FLAG_COLOR_MATCH -> "Match the Flag Color"
        TERRITORY_COMPARE -> "Bigger Territory"
        TRUE_FALSE -> "True or False"
        BLIND_MAP_BATTLE -> "Blind Map Battle"
        else -> key
    }

    fun color(key: String): Color = when (key) {
        YEAR_GUESS -> AppColors.SlateBlue
        CASUALTIES_MULTIPLIER -> AppColors.Maroon
        FLAG_COLOR_MATCH -> AppColors.OliveDark
        TERRITORY_COMPARE -> AppColors.ForestDark
        TRUE_FALSE -> AppColors.BrownDark
        BLIND_MAP_BATTLE -> AppColors.EspressoDark
        else -> AppColors.Tan
    }

    fun formatScore(key: String, score: Double): String = when (key) {
        CASUALTIES_MULTIPLIER -> "x%.2f".format(score)
        else -> "%.2f".format(score)
    }
}
