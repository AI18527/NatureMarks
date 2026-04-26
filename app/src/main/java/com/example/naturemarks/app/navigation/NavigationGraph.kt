package com.example.naturemarks.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.naturemarks.ui.screens.gallery.GalleryScreen
import com.example.naturemarks.ui.screens.markdetails.MarkDetailsScreen
import com.example.naturemarks.ui.screens.scan.ScanScreen
import com.example.naturemarks.ui.screens.welcome.WelcomeScreen
import com.example.naturemarks.ui.screens.welcome.WelcomeViewModel

@Composable
fun NavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable (Screen.Welcome.route) { backStackEntry ->
            val viewModel: WelcomeViewModel =
                viewModel(backStackEntry)

            WelcomeScreen(
                viewModel = viewModel,
                onOpenGallery = {
                    navController.navigate(
                        Screen.Gallery.route
                    )
                },
                onOpenScan = {
                    navController.navigate(Screen.Scan.route)
                }
            )
        }

        composable(Screen.Gallery.route) {
            GalleryScreen(
                onMarkClicked = {
                    navController.navigate(
                        Screen.MarkDetails.createRoute(it)
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MarkDetails.route,
            arguments = listOf(navArgument("markId") {
                type = NavType.StringType
            })
        ){ backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Gallery.route)
            }

            val markId =
                backStackEntry.arguments?.getString("markId") ?: ""

            MarkDetailsScreen(
                markId = markId,
            ) { navController.popBackStack() }
        }

        composable(Screen.Scan.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Welcome.route)
            }

            ScanScreen(
                onBack = { navController.popBackStack() },
                onOpenGallery = {
                    navController.navigate(Screen.Gallery.route) {
                        popUpTo(Screen.Scan.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }

}