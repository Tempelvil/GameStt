package com.example.gamest.model


data class GameUiModel(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val rating: Double,
    val genres: List<String>,
    val platforms: List<String>,
    val isSaved: Boolean = false,
    val status: GameStatus? = null,
    val metacritic: Int? = null
)
data class GameDetailsUiModel(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val description: String,
    val releaseDate: String?,
    val rating: Double,
    val metacritic: Int?,
    val genres: List<GameTagUiModel>,
    val platforms: List<String>,
    val developers: List<GameCompanyUiModel>,
    val publishers: List<GameCompanyUiModel>,
    val screenshots: List<String> = emptyList(),
    val isSaved: Boolean = false
)

data class GameTagUiModel(
    val id: Int,
    val name: String,
    val slug: String
)

data class GameCompanyUiModel(
    val id: Int,
    val name: String,
    val slug: String
)

enum class GameStatus {
    Played,
    Playing,
    Planned,
    Dropped
}
