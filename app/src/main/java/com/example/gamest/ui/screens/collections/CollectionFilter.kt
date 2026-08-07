package com.example.gamest.ui.screens.collections

import com.example.gamest.data.local.GameStatus

enum class CollectionFilter(
    val title: String,
    val status: GameStatus?
) {
    ALL(
        title = "All",
        status = null
    ),

    PLANNED(
        title = "Plan",
        status = GameStatus.PLANNED
    ),

    PLAYING(
        title = "Play",
        status = GameStatus.PLAYING
    ),

    COMPLETED(
        title = "Done",
        status = GameStatus.COMPLETED
    ),

    DROPPED(
        title = "Drop",
        status = GameStatus.DROPPED
    )
}