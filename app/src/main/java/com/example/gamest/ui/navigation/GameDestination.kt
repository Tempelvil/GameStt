package com.example.gamest.ui.navigation

object GameDestination{
    const val SEARCH = "search"
    const val COLLECTION = "collection"
    const val STATISTICS = "statistics"
    const val STEAM_LIBRARY = "steam_library"
    const val DETAILS_ARGUMENT = "gameId"
    const val STEAM_PLAYTIME_ARGUMENT = "steamPlaytimeMinutes"
    const val DETAILS =
        "details/{$DETAILS_ARGUMENT}?" +
            "$STEAM_PLAYTIME_ARGUMENT={$STEAM_PLAYTIME_ARGUMENT}"

    fun createDetailsRoute(
        gameId: Int,
        steamPlaytimeMinutes: Long? = null
    ): String {
        return buildString {
            append("details/")
            append(gameId)
            steamPlaytimeMinutes?.let { minutes ->
                append("?")
                append(STEAM_PLAYTIME_ARGUMENT)
                append("=")
                append(minutes.coerceAtLeast(0))
            }
        }
    }
}
