package com.example.naturemarks.app.navigation

import androidx.annotation.RequiresPermission
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
@RequiresPermission(allOf = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"])
fun NavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destination.Welcome.route
    ) {
        composable (Destination.Welcome.route) { backStackEntry ->
            val viewModel: WelcomeViewModel =
                viewModel(backStackEntry)

            WelcomeScreen(
                viewModel = viewModel,
                onOpenGallery = {
                    navController.navigate(
                        Destination.Gallery.route
                    )
                },
                onOpenScan = {
                    navController.navigate(Destination.Scan.route)
                }
            )
        }

        composable(Destination.Gallery.route) {
            GalleryScreen(
                onMarkClicked = {
                    navController.navigate(
                        Destination.MarkDetails.createRoute(it)
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destination.MarkDetails.route,
            arguments = listOf(navArgument("markId") {
                type = NavType.StringType
            })
        ){ backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destination.Gallery.route)
            }

            val markId =
                backStackEntry.arguments?.getString("markId") ?: ""

            MarkDetailsScreen(
                markId = markId,
            ) { navController.popBackStack() }
        }

        composable(Destination.Scan.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destination.Welcome.route)
            }
            ScanScreen(
                onBack = { navController.popBackStack() },
                onOpenGallery = {
                    navController.navigate(Destination.Gallery.route) {
                        popUpTo(Destination.Scan.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }

}