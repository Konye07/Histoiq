package com.konye.historiq.Core.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.konye.historiq.Core.Haptics.Haptics
import com.konye.historiq.Core.Localization.StringsHomeShared
import com.konye.historiq.Core.Network.GameGate
import com.konye.historiq.Core.Theme.AppColors
import com.konye.historiq.Core.Theme.contrastingOn
import com.konye.historiq.Core.Theme.lighten
import com.konye.historiq.Features.Friends.FriendsScreen
import com.konye.historiq.Features.Play.BlindMapBattle.BlindMapBattleScreen
import com.konye.historiq.Features.Play.CasualtiesMultiplier.CasualtiesMultiplierScreen
import com.konye.historiq.Features.Play.FlagColorMatch.FlagColorMatchScreen
import com.konye.historiq.Features.Play.PlayScreen
import com.konye.historiq.Features.Play.TerritoryCompare.TerritoryCompareScreen
import com.konye.historiq.Features.Play.TrueFalse.TrueFalseScreen
import com.konye.historiq.Features.Play.YearGuess.YearGuessScreen
import com.konye.historiq.Features.Profile.ProfileScreen
import com.konye.historiq.Features.Settings.PrivacyPolicyScreen
import com.konye.historiq.Features.Settings.SettingsScreen
import com.konye.historiq.generated.resources.Res
import com.konye.historiq.generated.resources.ic_people
import com.konye.historiq.generated.resources.ic_person
import com.konye.historiq.generated.resources.ic_play_arrow
import com.konye.historiq.generated.resources.ic_settings
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppNavHost(onSignedOut: () -> Unit = {}) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val gameColor = gameColorForRoute(currentRoute)
            // Falls back to the ambient AppTheme's own colors (not a hardcoded light-mode
            // constant) so the bar still matches on Play/Friends/Profile/Settings when dark mode
            // is on — no MaterialTheme override sits between AppTheme and this Scaffold, so these
            // already reflect the current light/dark scheme correctly.
            val barColor = gameColor ?: MaterialTheme.colorScheme.primaryContainer
            val onBarColor = if (gameColor != null) contrastingOn(gameColor) else MaterialTheme.colorScheme.onPrimaryContainer
            val indicatorColor = gameColor?.lighten(0.25f) ?: MaterialTheme.colorScheme.primary

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
                GameGate { YearGuessScreen(onBack = { navController.popBackStack() }) }
            }
            composable(NavRoutes.CasualtiesMultiplier.route) {
                GameGate { CasualtiesMultiplierScreen(onBack = { navController.popBackStack() }) }
            }
            composable(NavRoutes.FlagColorMatch.route) {
                GameGate { FlagColorMatchScreen(onBack = { navController.popBackStack() }) }
            }
            composable(NavRoutes.TerritoryCompare.route) {
                GameGate { TerritoryCompareScreen(onBack = { navController.popBackStack() }) }
            }
            composable(NavRoutes.TrueFalse.route) {
                GameGate { TrueFalseScreen(onBack = { navController.popBackStack() }) }
            }
            composable(NavRoutes.BlindMapBattle.route) {
                GameGate { BlindMapBattleScreen(onBack = { navController.popBackStack() }) }
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
