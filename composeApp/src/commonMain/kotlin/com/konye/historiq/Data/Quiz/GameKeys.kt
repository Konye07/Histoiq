package com.konye.historiq.Data.Quiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.konye.historiq.Core.Formatting.formatFixed
import com.konye.historiq.Core.Localization.StringsGamekeys
import com.konye.historiq.Core.Theme.AppColors

/** Central registry of the six quizzes' progress-store keys, so Friends/Profile/Stats agree with each game screen. */
object GameKeys {
    const val YEAR_GUESS = "year_guess"
    const val CASUALTIES_MULTIPLIER = "casualties_multiplier"
    const val FLAG_COLOR_MATCH = "flag_color_match"
    const val TERRITORY_COMPARE = "territory_compare"
    const val TRUE_FALSE = "true_false"
    const val BLIND_MAP_BATTLE = "blind_map_battle"

    val ALL = listOf(YEAR_GUESS, CASUALTIES_MULTIPLIER, FLAG_COLOR_MATCH, TERRITORY_COMPARE, TRUE_FALSE, BLIND_MAP_BATTLE)

    @Composable
    fun roleName(key: String): String = when (key) {
        YEAR_GUESS -> StringsGamekeys.role_annalist
        CASUALTIES_MULTIPLIER -> StringsGamekeys.role_herald
        FLAG_COLOR_MATCH -> StringsGamekeys.role_officer_of_arms
        TERRITORY_COMPARE -> StringsGamekeys.role_cosmographer
        TRUE_FALSE -> StringsGamekeys.role_chronicler
        BLIND_MAP_BATTLE -> StringsGamekeys.role_chancellor
        else -> key
    }

    @Composable
    fun title(key: String): String = when (key) {
        YEAR_GUESS -> StringsGamekeys.title_year_guess
        CASUALTIES_MULTIPLIER -> StringsGamekeys.title_casualties_multiplier
        FLAG_COLOR_MATCH -> StringsGamekeys.title_flag_color_match
        TERRITORY_COMPARE -> StringsGamekeys.title_territory_compare
        TRUE_FALSE -> StringsGamekeys.title_true_false
        BLIND_MAP_BATTLE -> StringsGamekeys.title_blind_map_battle
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
        CASUALTIES_MULTIPLIER -> "x" + formatFixed(score, 2)
        else -> formatFixed(score, 2)
    }
}
