package com.azhdar.app10.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.azhdar.app10.core.ads.AdsManager
import com.azhdar.app10.ui.FlashlightViewModel
import com.azhdar.app10.ui.screens.ColorChooserScreen
import com.azhdar.app10.ui.screens.ColorPickerScreen
import com.azhdar.app10.ui.screens.HomeScreen
import com.azhdar.app10.ui.screens.SolidColorScreen

/**
 * Navigation graph.
 *
 * The original app declared five raw string routes, two of which ("ali" and "alireza") were
 * unreachable experiment screens that only navigated around. The graph is now explicit and
 * the selected colour travels as a navigation argument.
 */
@Composable
fun AppNavHost(
    viewModel: FlashlightViewModel,
    adsManager: AdsManager,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route,
        modifier = modifier,
        enterTransition = {
            slideInVertically(tween(300)) { it / 6 } + fadeIn(tween(300))
        },
        exitTransition = {
            slideOutVertically(tween(300)) { -it / 8 } + fadeOut(tween(300))
        },
        popEnterTransition = {
            slideInVertically(tween(300)) { -it / 8 } + fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutVertically(tween(300)) { it / 6 } + fadeOut(tween(300))
        },
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                adsManager = adsManager,
                onOpenColorChooser = { navController.navigate(Routes.ColorChooser.route) },
                onOpenColorPicker = { navController.navigate(Routes.ColorPicker.route) },
            )
        }

        composable(Routes.ColorChooser.route) {
            ColorChooserScreen(
                onColorSelected = { color ->
                    navController.navigate(Routes.SolidColor.createRoute(color))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ColorPicker.route) {
            ColorPickerScreen(
                onColorSelected = { color ->
                    navController.navigate(Routes.SolidColor.createRoute(color))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.SolidColor.route,
            arguments = listOf(
                navArgument(Routes.COLOR_ARG) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val color = backStackEntry.arguments?.getInt(Routes.COLOR_ARG) ?: 0xFFFFFFFF.toInt()
            SolidColorScreen(
                color = color,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
