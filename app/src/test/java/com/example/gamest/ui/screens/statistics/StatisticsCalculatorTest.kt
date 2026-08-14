package com.example.gamest.ui.screens.statistics

import com.example.gamest.data.local.GameEntity
import com.example.gamest.data.local.GameStatus
import com.example.gamest.data.local.StoredTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsCalculatorTest {

    @Test
    fun emptyCollectionProducesEmptyState() {
        val state = StatisticsCalculator.calculate(
            games = emptyList(),
            selectedSection = StatisticsSection.PLAYED_GAMES
        )

        assertTrue(state.isEmpty)
        assertFalse(state.isLoading)
        assertEquals(0, state.playedGamesCount)
        assertNull(state.averageRating)
    }

    @Test
    fun playedGamesExcludeOnlyPlannedGames() {
        val games = listOf(
            game(id = 1, status = GameStatus.PLANNED),
            game(id = 2, status = GameStatus.PLAYING),
            game(id = 3, status = GameStatus.COMPLETED),
            game(id = 4, status = GameStatus.DROPPED)
        )

        val state = calculate(games)

        assertEquals(3, state.playedGamesCount)
        assertEquals(1, state.completedGamesCount)
    }

    @Test
    fun averageRatingIgnoresGamesWithoutRating() {
        val games = listOf(
            game(id = 1, userRating = 10),
            game(id = 2, userRating = 8),
            game(id = 3, userRating = null)
        )

        val state = calculate(games)

        assertEquals(9.0, state.averageRating ?: 0.0, 0.001)
        assertEquals(2, state.ratedGamesCount)
        assertEquals(listOf(10, 8), state.highestRatedGames.map { it.userRating })
    }

    @Test
    fun gamesAreSortedByPlaytimeDescending() {
        val games = listOf(
            game(id = 1, title = "Middle", hoursPlayed = 40),
            game(id = 2, title = "Longest", hoursPlayed = 120),
            game(id = 3, title = "No time", hoursPlayed = 0),
            game(id = 4, title = "Short", hoursPlayed = 10)
        )

        val state = calculate(games)

        assertEquals(170, state.totalHours)
        assertEquals(
            listOf("Longest", "Middle", "Short"),
            state.gamesByPlaytime.map { it.title }
        )
    }

    @Test
    fun multiGenreGameAddsFullHoursToEveryGenreAndPercentsSumToOneHundred() {
        val games = listOf(
            game(
                id = 1,
                hoursPlayed = 100,
                genres = listOf("RPG", "Action")
            ),
            game(
                id = 2,
                hoursPlayed = 100,
                genres = listOf("RPG")
            )
        )

        val state = calculate(games)
        val rpg = state.genrePlaytime.first { it.name == "RPG" }
        val action = state.genrePlaytime.first { it.name == "Action" }

        assertEquals(200, rpg.hours)
        assertEquals(100, action.hours)
        assertEquals(100, state.genrePlaytime.sumOf { it.percent })
        assertEquals("RPG", state.favoriteGenre)
        assertEquals(rpg.percent, state.favoriteGenrePercent)
    }

    @Test
    fun duplicateGenreInsideOneGameIsCountedOnce() {
        val game = game(
            id = 1,
            hoursPlayed = 50,
            genres = listOf("RPG", "rpg")
        )

        val state = calculate(listOf(game))

        assertEquals(1, state.genrePlaytime.size)
        assertEquals(50, state.genrePlaytime.single().hours)
        assertEquals(100, state.genrePlaytime.single().percent)
    }

    private fun calculate(
        games: List<GameEntity>
    ): StatisticsUiState {
        return StatisticsCalculator.calculate(
            games = games,
            selectedSection = StatisticsSection.PLAYED_GAMES
        )
    }

    private fun game(
        id: Int,
        title: String = "Game $id",
        status: GameStatus = GameStatus.PLAYING,
        userRating: Int? = null,
        hoursPlayed: Int = 0,
        genres: List<String> = emptyList()
    ): GameEntity {
        return GameEntity(
            id = id,
            title = title,
            imageUrl = null,
            description = "",
            releaseDate = "",
            communityRating = 0.0,
            criticRating = null,
            hastilySeconds = null,
            normallySeconds = null,
            completelySeconds = null,
            genres = genres.mapIndexed { index, name ->
                StoredTag(
                    id = index,
                    name = name,
                    slug = name.lowercase()
                )
            },
            platforms = emptyList(),
            developers = emptyList(),
            publishers = emptyList(),
            screenshots = emptyList(),
            ageRating = null,
            status = status,
            userRating = userRating,
            hoursPlayed = hoursPlayed
        )
    }
}
