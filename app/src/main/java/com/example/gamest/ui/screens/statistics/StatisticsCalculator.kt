package com.example.gamest.ui.screens.statistics

import com.example.gamest.data.local.GameEntity
import com.example.gamest.data.local.GameStatus
import kotlin.math.floor

object StatisticsCalculator {

    fun calculate(
        games: List<GameEntity>,
        selectedSection: StatisticsSection
    ): StatisticsUiState {
        if (games.isEmpty()) {
            return StatisticsUiState(
                selectedSection = selectedSection,
                isLoading = false,
                isEmpty = true
            )
        }

        val playedGames = games.filter { game ->
            game.status != GameStatus.PLANNED
        }
        val completedGames = games
            .filter { game -> game.status == GameStatus.COMPLETED }
            .sortedByDescending { game -> game.updatedAt }

        val ratedGames = games.filter { game ->
            game.userRating != null
        }
        val gamesByPlaytime = games
            .filter { game -> game.hoursPlayed > 0 }
            .sortedWith(
                compareByDescending<GameEntity> { game -> game.hoursPlayed }
                    .thenBy { game -> game.title.lowercase() }
            )

        val totalHours = games.sumOf { game ->
            game.hoursPlayed.coerceAtLeast(0)
        }
        val genrePlaytime = calculateGenrePlaytime(games)
        val favoriteGenre = genrePlaytime.firstOrNull()

        return StatisticsUiState(
            playedGamesCount = playedGames.size,
            completedGamesCount = completedGames.size,
            averageRating = ratedGames
                .mapNotNull { game -> game.userRating }
                .average()
                .takeUnless { average -> average.isNaN() },
            ratedGamesCount = ratedGames.size,
            totalHours = totalHours,
            gamesWithPlaytimeCount = gamesByPlaytime.size,
            favoriteGenre = favoriteGenre?.name,
            favoriteGenrePercent = favoriteGenre?.percent,
            statusStatistics = GameStatus.entries.map { status ->
                StatusStatisticUiModel(
                    status = status,
                    title = status.displayName(),
                    count = games.count { game -> game.status == status }
                )
            },
            completedGames = completedGames.map(GameEntity::toStatisticUiModel),
            ratingDistribution = (10 downTo 1).map { rating ->
                RatingStatisticUiModel(
                    rating = rating,
                    count = ratedGames.count { game ->
                        game.userRating == rating
                    }
                )
            },
            highestRatedGames = ratedGames
                .sortedWith(
                    compareByDescending<GameEntity> { game -> game.userRating }
                        .thenBy { game -> game.title.lowercase() }
                )
                .map(GameEntity::toStatisticUiModel),
            gamesByPlaytime = gamesByPlaytime.map(GameEntity::toStatisticUiModel),
            genrePlaytime = genrePlaytime,
            favoriteGenreGames = favoriteGenre
                ?.let { genre ->
                    gamesByPlaytime
                        .filter { game ->
                            game.genres.any { tag ->
                                tag.name.equals(
                                    other = genre.name,
                                    ignoreCase = true
                                )
                            }
                        }
                        .map(GameEntity::toStatisticUiModel)
                }
                .orEmpty(),
            selectedSection = selectedSection,
            isLoading = false,
            isEmpty = false
        )
    }

    private fun calculateGenrePlaytime(
        games: List<GameEntity>
    ): List<GenrePlaytimeUiModel> {
        val hoursByGenre = mutableMapOf<String, Int>()

        games
            .filter { game -> game.hoursPlayed > 0 }
            .forEach { game ->
                game.genres
                    .distinctBy { genre -> genre.name.lowercase() }
                    .forEach { genre ->
                        hoursByGenre[genre.name] =
                            hoursByGenre.getOrDefault(genre.name, 0) +
                                    game.hoursPlayed
                    }
            }

        val totalGenreHours = hoursByGenre.values.sum()
        if (totalGenreHours == 0) {
            return emptyList()
        }

        val sortedGenres = hoursByGenre.entries.sortedWith(
            compareByDescending<Map.Entry<String, Int>> { entry -> entry.value }
                .thenBy { entry -> entry.key.lowercase() }
        )
        val exactPercentages = sortedGenres.map { entry ->
            entry.value.toDouble() / totalGenreHours * 100
        }
        val percentages = exactPercentages
            .map { percent -> floor(percent).toInt() }
            .toMutableList()
        val remainderOrder = exactPercentages.indices.sortedWith(
            compareByDescending<Int> { index ->
                exactPercentages[index] - percentages[index]
            }.thenBy { index -> sortedGenres[index].key.lowercase() }
        )

        repeat(100 - percentages.sum()) { offset ->
            val index = remainderOrder[offset % remainderOrder.size]
            percentages[index] += 1
        }

        return sortedGenres.mapIndexed { index, entry ->
            GenrePlaytimeUiModel(
                name = entry.key,
                hours = entry.value,
                percent = percentages[index]
            )
        }
    }
}

private fun GameEntity.toStatisticUiModel(): StatisticGameUiModel {
    return StatisticGameUiModel(
        id = id,
        title = title,
        imageUrl = imageUrl,
        status = status,
        userRating = userRating,
        hoursPlayed = hoursPlayed
    )
}

private fun GameStatus.displayName(): String {
    return when (this) {
        GameStatus.PLANNED -> "Planned"
        GameStatus.PLAYING -> "Playing"
        GameStatus.COMPLETED -> "Completed"
        GameStatus.DROPPED -> "Dropped"
    }
}
