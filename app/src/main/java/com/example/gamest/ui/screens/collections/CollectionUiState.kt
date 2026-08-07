package com.example.gamest.ui.screens.collections

import com.example.gamest.data.local.GameEntity
import com.example.gamest.ui.screens.collection.CollectionSort

data class CollectionUiState(
    val games: List<GameEntity> = emptyList(),

    val selectedFilter: CollectionFilter =
        CollectionFilter.ALL,

    val selectedSort: CollectionSort =
        CollectionSort.RECENTLY_ADDED,

    val isLoading: Boolean = true
)