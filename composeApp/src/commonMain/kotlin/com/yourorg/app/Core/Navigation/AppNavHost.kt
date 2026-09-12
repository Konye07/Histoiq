package com.yourorg.app.Core.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourorg.app.Core.Haptics.Haptics
import com.yourorg.app.Core.Localization.StringsHomeShared
import com.yourorg.app.Core.Theme.AppColors
import com.yourorg.app.Core.Theme.contrastingOn
import com.yourorg.app.Core.Theme.lighten
import com.yourorg.app.Features.Friends.FriendsScreen
import com.yourorg.app.Features.Play.BlindMapBattle.BlindMapBattleScreen
import com.yourorg.app.Features.Play.CasualtiesMultiplier.CasualtiesMultiplierScreen
import com.yourorg.app.Features.Play.FlagColorMatch.FlagColorMatchScreen
import com.yourorg.app.Features.Play.PlayScreen
import com.yourorg.app.Features.Play.TerritoryCompare.TerritoryCompareScreen
import com.yourorg.app.Features.Play.TrueFalse.TrueFalseScreen
import com.yourorg.app.Features.Play.YearGuess.YearGuessScreen
import com.yourorg.app.Features.Profile.ProfileScreen
import com.yourorg.app.Features.Settings.PrivacyPolicyScreen
import com.yourorg.app.Features.Settings.SettingsScreen
import com.yourorg.app.generated.resources.Res
import com.yourorg.app.generated.resources.ic_people
import com.yourorg.app.generated.resources.ic_person
import com.yourorg.app.generated.resources.ic_play_arrow
import com.yourorg.app.generated.resources.ic_settings
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppNavHost(onSignedOut: () -> Unit = {}) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val gameColor = gameColorForRoute(currentRoute)
            val barColor = gameColor ?: AppColors.Tan.lighten(0.35f)
            val onBarColor = if (gameColor != null) contrastingOn(gameColor) else AppColors.EspressoDark
            val indicatorColor = gameColor?.lighten(0.25f) ?: AppColors.Tan

            NavigationBar(containerColor = barColor) {
                NavRoutes.bottomTabs.forEach { tab ->
                    val label = labelFor(tab)
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            Haptics.tick()
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(tab), contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = onBarColor,
                            selectedTextColor = onBarColor,
                            indicatorColor = indicatorColor,
                            unselectedIconColor = onBarColor.copy(alpha = 0.6f),
                            unselectedTextColor = onBarColor.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Play.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.Play.route) {
                PlayScreen(
                    onSelectYearGuess = { navController.navigate(NavRoutes.YearGuess.route) },
                    onSelectCasualtiesMultiplier = { navController.navigate(NavRoutes.CasualtiesMultiplier.route) },
                    onSelectFlagColorMatch = { navController.navigate(NavRoutes.FlagColorMatch.route) },
                    onSelectTerritoryCompare = { navController.navigate(NavRoutes.TerritoryCompare.route) },
                    onSelectTrueFalse = { navController.navigate(NavRoutes.TrueFalse.route) },
                    onSelectBlindMapBattle = { navController.navigate(NavRoutes.BlindMapBattle.route) }
                )
            }
            composable(NavRoutes.YearGuess.route) {
                YearGuessScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.CasualtiesMultiplier.route) {
                CasualtiesMultiplierScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.FlagColorMatch.route) {
                FlagColorMatchScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.TerritoryCompare.route) {
                TerritoryCompareScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.TrueFalse.route) {
                TrueFalseScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.BlindMapBattle.route) {
                BlindMapBattleScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.Friends.route) { FriendsScreen() }
            composable(NavRoutes.Profile.route) { ProfileScreen() }
            composable(NavRoutes.Settings.route) {
                SettingsScreen(
                    onSignedOut = onSignedOut,
                    onOpenPrivacyPolicy = { navController.navigate(NavRoutes.PrivacyPolicy.route) }
                )
            }
            composable(NavRoutes.PrivacyPolicy.route) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun gameColorForRoute(route: String?): Color? = when (route) {
    NavRoutes.YearGuess.route -> AppColors.SlateBlue
    NavRoutes.CasualtiesMultiplier.route -> AppColors.Maroon
    NavRoutes.FlagColorMatch.route -> AppColors.OliveDark
    NavRoutes.TerritoryCompare.route -> AppColors.ForestDark
    NavRoutes.TrueFalse.route -> AppColors.BrownDark
    NavRoutes.BlindMapBattle.route -> AppColors.EspressoDark
    else -> null
}

@Composable
private fun labelFor(route: NavRoutes): String = when (route) {
    NavRoutes.Play -> StringsHomeShared.nav_play
    NavRoutes.Friends -> StringsHomeShared.nav_friends
    NavRoutes.Profile -> StringsHomeShared.nav_profile
    NavRoutes.Settings -> StringsHomeShared.nav_settings
    NavRoutes.YearGuess -> StringsHomeShared.nav_year_guess
    NavRoutes.CasualtiesMultiplier -> StringsHomeShared.nav_casualties_multiplier
    NavRoutes.FlagColorMatch -> StringsHomeShared.nav_flag_color_match
    NavRoutes.TerritoryCompare -> StringsHomeShared.nav_territory_compare
    NavRoutes.TrueFalse -> StringsHomeShared.nav_true_false
    NavRoutes.BlindMapBattle -> StringsHomeShared.nav_blind_map_battle
    NavRoutes.PrivacyPolicy -> StringsHomeShared.nav_settings
}

@Composable
private fun iconFor(route: NavRoutes) = when (route) {
    NavRoutes.Play -> painterResource(Res.drawable.ic_play_arrow)
    NavRoutes.Friends -> painterResource(Res.drawable.ic_people)
    NavRoutes.Profile -> painterResource(Res.drawable.ic_person)
    NavRoutes.Settings -> painterResource(Res.drawable.ic_settings)
    NavRoutes.YearGuess,
    NavRoutes.CasualtiesMultiplier,
    NavRoutes.FlagColorMatch,
    NavRoutes.TerritoryCompare,
    NavRoutes.TrueFalse,
    NavRoutes.BlindMapBattle -> painterResource(Res.drawable.ic_play_arrow)
    NavRoutes.PrivacyPolicy -> painterResource(Res.drawable.ic_settings)
}
