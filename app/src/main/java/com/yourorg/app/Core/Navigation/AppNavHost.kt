package com.yourorg.app.Core.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourorg.app.Core.Haptics.Haptics
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
import com.yourorg.app.Features.Settings.SettingsScreen

@Composable
fun AppNavHost(onSignedOut: () -> Unit = {}) {
    val navController = rememberNavController()
    val context = LocalContext.current

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
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            Haptics.tick(context)
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(tab), contentDescription = tab.label) },
                        label = { Text(tab.label) },
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
            composable(NavRoutes.Settings.route) { SettingsScreen(onSignedOut = onSignedOut) }
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

private fun iconFor(route: NavRoutes) = when (route) {
    NavRoutes.Play -> Icons.Default.PlayArrow
    NavRoutes.Friends -> Icons.Default.People
    NavRoutes.Profile -> Icons.Default.Person
    NavRoutes.Settings -> Icons.Default.Settings
    NavRoutes.YearGuess,
    NavRoutes.CasualtiesMultiplier,
    NavRoutes.FlagColorMatch,
    NavRoutes.TerritoryCompare,
    NavRoutes.TrueFalse,
    NavRoutes.BlindMapBattle -> Icons.Default.PlayArrow
}
