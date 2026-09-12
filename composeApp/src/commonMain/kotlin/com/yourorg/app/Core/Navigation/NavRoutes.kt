package com.yourorg.app.Core.Navigation

sealed class NavRoutes(val route: String) {
    data object Play : NavRoutes("play")
    data object Friends : NavRoutes("friends")
    data object Profile : NavRoutes("profile")
    data object Settings : NavRoutes("settings")
    data object YearGuess : NavRoutes("play/year_guess")
    data object CasualtiesMultiplier : NavRoutes("play/casualties_multiplier")
    data object FlagColorMatch : NavRoutes("play/flag_color_match")
    data object TerritoryCompare : NavRoutes("play/territory_compare")
    data object TrueFalse : NavRoutes("play/true_false")
    data object BlindMapBattle : NavRoutes("play/blind_map_battle")
    data object PrivacyPolicy : NavRoutes("settings/privacy_policy")

    companion object {
        val bottomTabs = listOf(Play, Friends, Profile, Settings)
    }
}
