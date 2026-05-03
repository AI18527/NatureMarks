package com.example.naturemarks.app.navigation

sealed class Destination(val route: String) {
    object Welcome : Destination("welcome")

    object Gallery : Destination("gallery")

    object MarkDetails : Destination("markDetails/{markId}") {
        fun createRoute(markId: String): String =
            "markDetails/$markId"
    }

    object Scan : Destination("scan")
}