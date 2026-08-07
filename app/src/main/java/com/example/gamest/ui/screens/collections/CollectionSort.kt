package com.example.gamest.ui.screens.collection

import com.example.gamest.data.local.GameSort

enum class CollectionSort(
    val title: String,
    val gameSort: GameSort
) {

    RECENTLY_ADDED(
        title = "Recently added",
        gameSort = GameSort.RECENTLY_ADDED
    ),

    TITLE(
        title = "Title",
        gameSort = GameSort.TITLE
    ),

    USER_RATING(
        title = "Your rating",
        gameSort = GameSort.USER_RATING
    ),

    HOURS_PLAYED(
        title = "Hours played",
        gameSort = GameSort.HOURS_PLAYED
    )
}