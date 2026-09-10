package com.yourorg.app.Core.Navigation

import androidx.annotation.StringRes
import com.yourorg.app.R

sealed class NavRoutes(val route: String, @StringRes val labelRes: Int) {
    data object Play : NavRoutes("play", R.string.nav_play)
    data object Friends : NavRoutes("friends", R.string.nav_friends)
    data object Profile : NavRoutes("profile", R.string.nav_profile)
    data object Settings : NavRoutes("settings", R.string.nav_settings)
    data object YearGuess : NavRoutes("play/year_guess", R.string.nav_year_guess)
    data object CasualtiesMultiplier : NavRoutes("play/casualties_multiplier", R.string.nav_casualties_multiplier)
    data object FlagColorMatch : NavRoutes("play/flag_color_match", R.string.nav_flag_color_match)
    data object TerritoryCompare : NavRoutes("play/territory_compare", R.string.nav_territory_compare)
    data object TrueFalse : NavRoutes("play/true_false", R.string.nav_true_false)
    data object BlindMapBattle : NavRoutes("play/blind_map_battle", R.string.nav_blind_map_battle)

    companion object {
        val bottomTabs = listOf(Play, Friends, Profile, Settings)
    }
}
