package com.example.gamest.ui.screens.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import com.example.gamest.data.local.GameEntity
import com.example.gamest.data.local.GameStatus
import com.example.gamest.ui.theme.GameStTheme


@Composable
fun StatisticsScreen(
    uiState: StatisticsUiState,
    onSectionClick: (StatisticsSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                top = 16.dp,
                bottom = 16.dp
            )
        )

        StatisticsSummaryGrid(
            uiState = uiState,
            onSectionClick = onSectionClick
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        StatisticsDetailsSection(
            uiState = uiState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}
@Composable
fun StatisticsSummaryGrid(
    uiState: StatisticsUiState,
    onSectionClick: (StatisticsSection) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            StatisticsSummaryCard(
                title = "Played Games",
                value = uiState.playedGamesCount.toString(),
                selected =
                    uiState.selectedSection ==
                            StatisticsSection.PLAYED_GAMES,
                onClick = {
                    onSectionClick(
                        StatisticsSection.PLAYED_GAMES
                    )
                },
                modifier = Modifier.weight(1f)
            )

            StatisticsSummaryCard(
                title = "Average Rating",
                value = uiState.averageRating
                    ?.let { rating ->
                        "%.1f / 10".format(rating)
                    }
                    ?: "—",
                selected =
                    uiState.selectedSection ==
                            StatisticsSection.AVERAGE_RATING,
                onClick = {
                    onSectionClick(
                        StatisticsSection.AVERAGE_RATING
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            StatisticsSummaryCard(
                title = "Total Hours",
                value = "${uiState.totalHours} h",
                selected =
                    uiState.selectedSection ==
                            StatisticsSection.TOTAL_HOURS,
                onClick = {
                    onSectionClick(
                        StatisticsSection.TOTAL_HOURS
                    )
                },
                modifier = Modifier.weight(1f)
            )

            StatisticsSummaryCard(
                title = "Favorite Genre",
                value = uiState.favoriteGenre ?: "—",
                selected =
                    uiState.selectedSection ==
                            StatisticsSection.FAVORITE_GENRE,
                onClick = {
                    onSectionClick(
                        StatisticsSection.FAVORITE_GENRE
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
fun StatisticsSummaryCard(
    title: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            16.dp
        ),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
                .copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (selected) {
                2.dp
            } else {
                1.dp
            },
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
                    .copy(alpha = 0.3f)
            }
        ),
        shadowElevation = 2.dp
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
@Composable
private fun PlayedGamesStatistics(
    games: List<GameEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            StatisticsSectionHeader(
                title = "Played Games",
                subtitle = if (games.size == 1) {
                    "1 game in your collection"
                } else {
                    "${games.size} games in your collection"
                }
            )
        }

        items(
            items = games,
            key = { game -> game.id }
        ) { game ->

            GameStatisticRow(
                title = game.title,
                value = game.userRating
                    ?.let { "$it / 10" }
                    ?: "Not rated"
            )
        }
    }
}
@Composable
private fun HoursStatistics(
    games: List<GameEntity>,
    modifier: Modifier = Modifier
) {
    val sortedGames = games
        .sortedByDescending { game ->
            game.hoursPlayed
        }

    val totalHours = games.sumOf { game ->
        game.hoursPlayed
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            StatisticsSectionHeader(
                title = "Total Hours",
                subtitle = "$totalHours hours played"
            )
        }

        items(
            items = sortedGames,
            key = { game -> game.id }
        ) { game ->

            GameStatisticRow(
                title = game.title,
                value = "${game.hoursPlayed} h"
            )
        }
    }
}

@Composable
private fun RatingStatistics(
    games: List<GameEntity>,
    modifier: Modifier = Modifier
) {
    val ratings = games
        .mapNotNull { game ->
            game.userRating
        }

    val averageRating = if (ratings.isNotEmpty()) {
        ratings.average()
    } else {
        null
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            StatisticsSectionHeader(
                title = "Average Rating",
                subtitle = averageRating
                    ?.let { rating ->
                        "%.1f / 10 • ${ratings.size} rated games"
                            .format(rating)
                    }
                    ?: "No rated games"
            )
        }

        items(
            items = (10 downTo 1).toList()
        ) { rating ->

            val count = ratings.count { userRating ->
                userRating == rating
            }

            val progress = if (ratings.isNotEmpty()) {
                count.toFloat() / ratings.size
            } else {
                0f
            }

            val percentage = (progress * 100).toInt()

            RatingRow(
                rating = rating,
                percentage = percentage,
                progress = progress
            )
        }
    }
}

@Composable
private fun RatingRow(
    rating: Int,
    percentage: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = rating.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.size(width = 28.dp, height = 24.dp)
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(100.dp))
        )

        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun GenreStatistics(
    games: List<GameEntity>,
    modifier: Modifier = Modifier
) {
    val previewGenres = listOf(
        "RPG" to 0.42f,
        "Action" to 0.34f,
        "Adventure" to 0.28f,
        "Shooter" to 0.21f,
        "Strategy" to 0.16f,
        "Indie" to 0.12f,
        "Racing" to 0.08f,
        "Simulation" to 0.06f
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            StatisticsSectionHeader(
                title = "Favorite Genres",
                subtitle = "Genres in your collection"
            )
        }

        items(previewGenres) { (genre, progress) ->

            GenreRow(
                genre = genre,
                progress = progress,
                percentage = (progress * 100).toInt()
            )
        }
    }
}
@Composable
private fun GenreRow(
    genre: String,
    progress: Float,
    percentage: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = genre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(100.dp))
        )
    }
}

@Composable
private fun StatisticsSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GameStatisticRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
            .copy(alpha = 0.35f)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}


@Composable
fun StatisticsDetailsSection(
    uiState: StatisticsUiState,
    modifier: Modifier = Modifier
) {
    when (uiState.selectedSection) {

        StatisticsSection.PLAYED_GAMES -> {
            PlayedGamesStatistics(
                games = uiState.games,
                modifier = modifier
            )
        }

        StatisticsSection.AVERAGE_RATING -> {
            RatingStatistics(
                games = uiState.games,
                modifier = modifier
            )
        }

        StatisticsSection.TOTAL_HOURS -> {
            HoursStatistics(
                games = uiState.games,
                modifier = modifier
            )
        }

        StatisticsSection.FAVORITE_GENRE -> {
            GenreStatistics(
                games = uiState.games,
                modifier = modifier
            )
        }
    }
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun StatisticsScreenPreview() {

    val games = listOf(
        previewGame(
            id = 1,
            title = "The Witcher 3",
            rating = 10,
            hours = 126
        ),
        previewGame(
            id = 2,
            title = "Elden Ring",
            rating = 9,
            hours = 84
        ),
        previewGame(
            id = 3,
            title = "Cyberpunk 2077",
            rating = 9,
            hours = 61
        ),
        previewGame(
            id = 4,
            title = "Hades",
            rating = 8,
            hours = 38
        ),
        previewGame(
            id = 5,
            title = "Baldur's Gate 3",
            rating = 10,
            hours = 143
        ),
        previewGame(
            id = 6,
            title = "Control",
            rating = 7,
            hours = 24
        )
    )

    GameStTheme {
        StatisticsScreen(
            uiState = StatisticsUiState(
                games = games,
                playedGamesCount = games.size,
                averageRating = games
                    .mapNotNull { it.userRating }
                    .average(),
                totalHours = games.sumOf { it.hoursPlayed },
                favoriteGenre = "RPG",
                selectedSection =
                    StatisticsSection.TOTAL_HOURS,
                isLoading = false
            ),
            onSectionClick = {}
        )
    }
}
private fun previewGame(
    id: Int,
    title: String,
    rating: Int?,
    hours: Int
): GameEntity {
    return GameEntity(
        id = id,
        title = title,
        imageUrl = null,
        description = "",
        releaseDate = "",
        ratingRawg = 0.0,
        metacritic = null,
        playtime = 0,
        genres = emptyList(),
        platforms = emptyList(),
        developers = emptyList(),
        publishers = emptyList(),
        screenshots = emptyList(),
        ageRating = null,
        userRating = rating,
        status = GameStatus.COMPLETED,
        hoursPlayed = hours
    )
}