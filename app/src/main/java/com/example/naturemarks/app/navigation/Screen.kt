package com.example.naturemarks.app.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")

    object Gallery : Screen("gallery")

    object MarkDetails : Screen("markDetails/{markId}") {
        fun createRoute(markId: String): String =
            "markDetails/$markId"
    }

    object Scan : Screen("scan")
}