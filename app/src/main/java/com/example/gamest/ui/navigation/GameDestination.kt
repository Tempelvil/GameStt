package com.example.gamest.ui.navigation

object GameDestination{
    const val SEARCH = "search"
    const val COLLECTION = "collection"
    const val STATISTICS = "statistics"
    const val STEAM_LIBRARY = "steam_library"
    const val DETAILS_ARGUMENT = "gameId"
    const val DETAILS = "details/{$DETAILS_ARGUMENT}"

    fun createDetailsRoute(gameId: Int): String {
        return "details/$gameId"
    }
}
