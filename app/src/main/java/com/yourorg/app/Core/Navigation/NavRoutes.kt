package com.yourorg.app.Core.Navigation

sealed class NavRoutes(val route: String, val label: String) {
    data object Play : NavRoutes("play", "Play")
    data object Friends : NavRoutes("friends", "Friends")
    data object Profile : NavRoutes("profile", "Profile")
    data object Settings : NavRoutes("settings", "Settings")
    data object YearGuess : NavRoutes("play/year_guess", "Guess the Year")
    data object CasualtiesMultiplier : NavRoutes("play/casualties_multiplier", "Casualty Count")
    data object FlagColorMatch : NavRoutes("play/flag_color_match", "Match the Flag Color")
    data object TerritoryCompare : NavRoutes("play/territory_compare", "Bigger Territory")
    data object TrueFalse : NavRoutes("play/true_false", "True or False")
    data object BlindMapBattle : NavRoutes("play/blind_map_battle", "Blind Map Battle")

    companion object {
        val bottomTabs = listOf(Play, Friends, Profile, Settings)
    }
}
