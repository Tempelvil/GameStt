package com.example.gamest.model


data class GameUiModel(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val communityRating: Double,
    val genres: List<String>,
    val platforms: List<String>,
    val isSaved: Boolean = false,
    val status: GameStatus? = null,
    val criticRating: Int? = null
)
data class GameDetailsUiModel(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val description: String,
    val releaseDate: String,
    val communityRating: Double,
    val criticRating: Int?,
    val genres: List<GameTagUiModel>,
    val platforms: List<String>,
    val platformDetails: List<GamePlatformUiModel> = emptyList(),
    val developers: List<GameCompanyUiModel>,
    val publishers: List<GameCompanyUiModel>,
    val screenshots: List<String> = emptyList(),
    val isSaved: Boolean = false,

    val ageRating: GameAgeRatingUiModel?,
    val timeToBeat: GameTimeToBeatUiModel = GameTimeToBeatUiModel(),
)

data class GamePage(
    val games: List<GameUiModel>,
    val hasMore: Boolean
)

data class GameTimeToBeatUiModel(
    val hastilySeconds: Int? = null,
    val normallySeconds: Int? = null,
    val completelySeconds: Int? = null,
    val submissionsCount: Int = 0
)
data class GameAgeRatingUiModel(
    val id: Int,
    val name: String,
    val slug: String
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

data class GamePlatformUiModel(
    val id: Int,
    val name: String,
    val abbreviation: String? = null
)

data class GamePlatformFamilyUiModel(
    val name: String,
    val platformIds: List<Int>
)

enum class GameStatus {
    Played,
    Playing,
    Planned,
    Dropped
}
