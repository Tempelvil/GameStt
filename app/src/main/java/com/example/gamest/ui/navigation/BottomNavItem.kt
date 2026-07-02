package com.example.gamest.ui.navigation

sealed class BottomNavItem(
    val route: String,
    val title: String
) {
    data object Search: BottomNavItem("search","Search")
    data object Collection: BottomNavItem("collection","Collection")
    data object Statistics: BottomNavItem("statistics", "Statistics")
}