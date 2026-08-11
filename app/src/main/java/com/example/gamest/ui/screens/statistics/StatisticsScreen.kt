package com.example.gamest.ui.screens.statistics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gamest.data.local.GameStatus
import com.example.gamest.ui.theme.GameStTheme
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    uiState: StatisticsUiState,
    onSectionClick: (StatisticsSection) -> Unit,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            StatisticsSummaryGrid(
                uiState = uiState,
                onSectionClick = onSectionClick
            )
        }

        when {
            uiState.isLoading -> {
                item {
                    StatisticsLoadingContent()
                }
            }

            uiState.isEmpty -> {
                item {
                    StatisticsEmptyContent()
                }
            }

            else -> {
                item {
                    when (uiState.selectedSection) {
                        StatisticsSection.PLAYED_GAMES ->
                            PlayedGamesContent(
                                uiState = uiState,
                                onGameClick = onGameClick
                            )

                        StatisticsSection.AVERAGE_RATING ->
                            AverageRatingContent(
                                uiState = uiState,
                                onGameClick = onGameClick
                            )

                        StatisticsSection.TOTAL_HOURS ->
                            TotalHoursContent(
                                uiState = uiState,
                                onGameClick = onGameClick
                            )

                        StatisticsSection.FAVORITE_GENRE ->
                            FavoriteGenreContent(
                                uiState = uiState,
                                onGameClick = onGameClick
                            )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsSummaryGrid(
    uiState: StatisticsUiState,
    onSectionClick: (StatisticsSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatisticsSummaryCard(
                title = "Played Games",
                value = uiState.playedGamesCount.toString(),
                supportingText = "${uiState.completedGamesCount} completed",
                icon = Icons.Default.SportsEsports,
                selected = uiState.selectedSection ==
                        StatisticsSection.PLAYED_GAMES,
                onClick = {
                    onSectionClick(StatisticsSection.PLAYED_GAMES)
                },
                accentColor = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )

            StatisticsSummaryCard(
                title = "Average Rating",
                value = uiState.averageRating.formatRating(),
                supportingText = when (uiState.ratedGamesCount) {
                    1 -> "1 rated game"
                    else -> "${uiState.ratedGamesCount} rated games"
                },
                icon = Icons.Default.Star,
                selected = uiState.selectedSection ==
                        StatisticsSection.AVERAGE_RATING,
                onClick = {
                    onSectionClick(StatisticsSection.AVERAGE_RATING)
                },
                accentColor = Color(0xFF00D8E8),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatisticsSummaryCard(
                title = "Total Hours",
                value = "${uiState.totalHours}h",
                supportingText = when (uiState.gamesWithPlaytimeCount) {
                    1 -> "1 game with playtime"
                    else ->
                        "${uiState.gamesWithPlaytimeCount} games with playtime"
                },
                icon = Icons.Default.AccessTime,
                selected = uiState.selectedSection ==
                        StatisticsSection.TOTAL_HOURS,
                onClick = {
                    onSectionClick(StatisticsSection.TOTAL_HOURS)
                },
                accentColor = Color(0xFF00C8D7),
                modifier = Modifier.weight(1f)
            )

            StatisticsSummaryCard(
                title = "Favorite Genre",
                value = uiState.favoriteGenre ?: "—",
                supportingText = uiState.favoriteGenrePercent
                    ?.let { percent -> "$percent% of playtime" }
                    ?: "No playtime data",
                icon = Icons.Default.Category,
                selected = uiState.selectedSection ==
                        StatisticsSection.FAVORITE_GENRE,
                onClick = {
                    onSectionClick(StatisticsSection.FAVORITE_GENRE)
                },
                accentColor = Color(0xFF9D4EDD),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatisticsSummaryCard(
    title: String,
    value: String,
    supportingText: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            accentColor
                .copy(alpha = 0.08f)
                .compositeOver(MaterialTheme.colorScheme.surface)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "summaryContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            accentColor
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
        },
        label = "summaryBorder"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(112.dp),
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = borderColor
        ),
        shadowElevation = if (selected) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = supportingText,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlayedGamesContent(
    uiState: StatisticsUiState,
    onGameClick: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatisticsSectionCard(
            title = "Games by Status"
        ) {
            if (uiState.statusStatistics.sumOf { it.count } == 0) {
                StatisticsSectionMessage("No games in collection")
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    StatusDonutChart(
                        statistics = uiState.statusStatistics,
                        modifier = Modifier.size(124.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.statusStatistics.forEachIndexed {
                                index,
                                statistic ->
                            StatusLegendRow(
                                statistic = statistic,
                                color = statusColors[index % statusColors.size]
                            )
                        }
                    }
                }
            }
        }

        StatisticsSectionCard(
            title = "Completed Games"
        ) {
            if (uiState.completedGames.isEmpty()) {
                StatisticsSectionMessage("No completed games yet")
            } else {
                StatisticGameList(
                    games = uiState.completedGames,
                    value = { game -> "${game.hoursPlayed}h" },
                    onGameClick = onGameClick
                )
            }
        }
    }
}

@Composable
private fun AverageRatingContent(
    uiState: StatisticsUiState,
    onGameClick: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatisticsSectionCard(
            title = "Rating Distribution"
        ) {
            val visibleRatings = uiState.ratingDistribution
                .filter { statistic -> statistic.count > 0 }

            if (visibleRatings.isEmpty()) {
                StatisticsSectionMessage("No rated games yet")
            } else {
                RatingDistribution(
                    statistics = visibleRatings
                )
            }
        }

        StatisticsSectionCard(
            title = "Highest Rated"
        ) {
            if (uiState.highestRatedGames.isEmpty()) {
                StatisticsSectionMessage("No rated games yet")
            } else {
                StatisticGameList(
                    games = uiState.highestRatedGames,
                    value = { game -> "${game.userRating}/10" },
                    onGameClick = onGameClick
                )
            }
        }
    }
}

@Composable
private fun TotalHoursContent(
    uiState: StatisticsUiState,
    onGameClick: (Int) -> Unit
) {
    StatisticsSectionCard(
        title = "Playtime by Game",
        subtitle = "Games with recorded playtime, highest first"
    ) {
        if (uiState.gamesByPlaytime.isEmpty()) {
            StatisticsSectionMessage("No playtime recorded yet")
        } else {
            StatisticGameList(
                games = uiState.gamesByPlaytime,
                value = { game -> "${game.hoursPlayed}h" },
                onGameClick = onGameClick,
                showRank = true
            )
        }
    }
}

@Composable
private fun FavoriteGenreContent(
    uiState: StatisticsUiState,
    onGameClick: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatisticsSectionCard(
            title = "Playtime by Genre",
            subtitle = "A multi-genre game's full time counts in every genre"
        ) {
            if (uiState.genrePlaytime.isEmpty()) {
                StatisticsSectionMessage("No genre playtime available")
            } else {
                GenrePlaytimeBars(
                    genres = uiState.genrePlaytime
                )
            }
        }

        StatisticsSectionCard(
            title = uiState.favoriteGenre
                ?.let { genre -> "Top $genre Games" }
                ?: "Top Genre Games"
        ) {
            if (uiState.favoriteGenreGames.isEmpty()) {
                StatisticsSectionMessage("No games with playtime available")
            } else {
                StatisticGameList(
                    games = uiState.favoriteGenreGames,
                    value = { game -> "${game.hoursPlayed}h" },
                    onGameClick = onGameClick
                )
            }
        }
    }
}

@Composable
private fun StatisticsSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            subtitle?.let { text ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun ColumnScope.StatisticGameList(
    games: List<StatisticGameUiModel>,
    value: (StatisticGameUiModel) -> String,
    onGameClick: (Int) -> Unit,
    showRank: Boolean = false
) {
    games.forEachIndexed { index, game ->
        if (index > 0) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
            )
        }

        StatisticGameRow(
            game = game,
            value = value(game),
            rank = if (showRank) index + 1 else null,
            onClick = { onGameClick(game.id) }
        )
    }
}

@Composable
private fun StatisticGameRow(
    game: StatisticGameUiModel,
    value: String,
    rank: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        rank?.let { number ->
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(24.dp)
            )
        }

        AsyncImage(
            model = game.imageUrl,
            contentDescription = game.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 46.dp, height = 54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = game.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = game.status.displayName(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "See more details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RatingDistribution(
    statistics: List<RatingStatisticUiModel>,
    modifier: Modifier = Modifier
) {
    val maximum = statistics.maxOfOrNull { statistic ->
        statistic.count
    }?.coerceAtLeast(1) ?: 1

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        statistics.forEach { statistic ->
            val targetProgress = statistic.count.toFloat() / maximum
            val animatedProgress by animateFloatAsState(
                targetValue = targetProgress,
                label = "ratingProgress"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statistic.rating.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(24.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00D8E8))
                    )
                }

                Text(
                    text = statistic.count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .width(34.dp)
                        .padding(start = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun GenrePlaytimeBars(
    genres: List<GenrePlaytimeUiModel>,
    modifier: Modifier = Modifier
) {
    val maximum = genres.maxOfOrNull { genre ->
        genre.hours
    }?.coerceAtLeast(1) ?: 1

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        genres.forEach { genre ->
            val progress by animateFloatAsState(
                targetValue = genre.hours.toFloat() / maximum,
                label = "genreProgress"
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = genre.name,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${genre.percent}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${genre.hours}h",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .width(54.dp)
                            .padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6))
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusDonutChart(
    statistics: List<StatusStatisticUiModel>,
    modifier: Modifier = Modifier
) {
    val total = statistics.sumOf { statistic -> statistic.count }

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.20f
        val inset = strokeWidth / 2f
        val arcSize = Size(
            width = size.width - strokeWidth,
            height = size.height - strokeWidth
        )

        if (total == 0) {
            drawArc(
                color = Color.Gray.copy(alpha = 0.25f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Butt
                )
            )
            return@Canvas
        }

        var startAngle = -90f
        statistics.forEachIndexed { index, statistic ->
            val sweepAngle = 360f * statistic.count / total

            if (sweepAngle > 0f) {
                drawArc(
                    color = statusColors[index % statusColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Butt
                    )
                )
            }

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun StatusLegendRow(
    statistic: StatusStatisticUiModel,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = statistic.title,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = statistic.count.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatisticsLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun StatisticsEmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No statistics yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Add games to your collection to see your statistics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatisticsSectionMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

private fun Double?.formatRating(): String {
    return this?.let { rating ->
        String.format(Locale.US, "%.1f", rating)
    } ?: "—"
}

private fun GameStatus.displayName(): String {
    return when (this) {
        GameStatus.PLANNED -> "Planned"
        GameStatus.PLAYING -> "Playing"
        GameStatus.COMPLETED -> "Completed"
        GameStatus.DROPPED -> "Dropped"
    }
}

private val statusColors = listOf(
    Color(0xFF64748B),
    Color(0xFF00D8E8),
    Color(0xFF8B5CF6),
    Color(0xFFEF476F)
)

@Preview(
    name = "Statistics interactive dark",
    showBackground = true,
    widthDp = 390,
    heightDp = 850
)
@Composable
private fun StatisticsInteractiveDarkPreview() {
    GameStTheme(darkTheme = true) {
        StatisticsPreviewHost()
    }
}

@Preview(
    name = "Statistics interactive light",
    showBackground = true,
    widthDp = 390,
    heightDp = 850
)
@Composable
private fun StatisticsInteractiveLightPreview() {
    GameStTheme(darkTheme = false) {
        StatisticsPreviewHost()
    }
}

@Composable
private fun StatisticsPreviewHost() {
    var previewState by remember {
        mutableStateOf(previewStatisticsState)
    }

    StatisticsScreen(
        uiState = previewState,
        onSectionClick = { section ->
            previewState = previewState.copy(
                selectedSection = section
            )
        },
        onGameClick = {}
    )
}

private val previewGames = listOf(
    StatisticGameUiModel(
        id = 1,
        title = "The Witcher 3",
        imageUrl = null,
        status = GameStatus.COMPLETED,
        userRating = 9,
        hoursPlayed = 112
    ),
    StatisticGameUiModel(
        id = 2,
        title = "Baldur's Gate 3",
        imageUrl = null,
        status = GameStatus.PLAYING,
        userRating = 10,
        hoursPlayed = 86
    ),
    StatisticGameUiModel(
        id = 3,
        title = "Elden Ring",
        imageUrl = null,
        status = GameStatus.COMPLETED,
        userRating = 9,
        hoursPlayed = 74
    ),
    StatisticGameUiModel(
        id = 4,
        title = "Cyberpunk 2077",
        imageUrl = null,
        status = GameStatus.PLAYING,
        userRating = 8,
        hoursPlayed = 62
    )
)

private val previewStatisticsState = StatisticsUiState(
    playedGamesCount = 27,
    completedGamesCount = 12,
    averageRating = 8.4,
    ratedGamesCount = 18,
    totalHours = 642,
    gamesWithPlaytimeCount = 24,
    favoriteGenre = "RPG",
    favoriteGenrePercent = 35,
    statusStatistics = listOf(
        StatusStatisticUiModel(GameStatus.PLANNED, "Planned", 8),
        StatusStatisticUiModel(GameStatus.PLAYING, "Playing", 4),
        StatusStatisticUiModel(GameStatus.COMPLETED, "Completed", 12),
        StatusStatisticUiModel(GameStatus.DROPPED, "Dropped", 3)
    ),
    completedGames = previewGames.filter { game ->
        game.status == GameStatus.COMPLETED
    },
    ratingDistribution = listOf(
        RatingStatisticUiModel(10, 3),
        RatingStatisticUiModel(9, 6),
        RatingStatisticUiModel(8, 5),
        RatingStatisticUiModel(7, 2)
    ),
    highestRatedGames = previewGames.sortedByDescending { game ->
        game.userRating
    },
    gamesByPlaytime = previewGames.sortedByDescending { game ->
        game.hoursPlayed
    },
    genrePlaytime = listOf(
        GenrePlaytimeUiModel("RPG", 225, 35),
        GenrePlaytimeUiModel("Action", 161, 25),
        GenrePlaytimeUiModel("Adventure", 128, 20),
        GenrePlaytimeUiModel("Strategy", 64, 10),
        GenrePlaytimeUiModel("Other", 64, 10)
    ),
    favoriteGenreGames = previewGames.take(3),
    selectedSection = StatisticsSection.PLAYED_GAMES,
    isLoading = false,
    isEmpty = false
)
