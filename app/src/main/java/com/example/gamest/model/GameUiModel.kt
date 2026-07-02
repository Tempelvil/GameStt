package com.example.gamest.model


data class GameUiModel(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val rating: Double,
    val genres: List<String>,
    val platforms: List<String>,
    val isSaved: Boolean = false,
    val status: GameStatus? = null
)

enum class GameStatus {
    Played,
    Playing,
    Planned,
    Dropped
}
