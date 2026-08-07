package com.example.gamest.ui.screens.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gamest.R
import com.example.gamest.data.local.GameEntity
import com.example.gamest.data.local.GameStatus
import com.example.gamest.data.local.StoredAgeRating
import com.example.gamest.data.local.StoredCompany
import com.example.gamest.data.local.StoredTag
import com.example.gamest.ui.screens.collections.CollectionFilter
import com.example.gamest.ui.screens.collections.CollectionUiState
import com.example.gamest.ui.theme.GameStTheme

@Composable
fun CollectionScreen(
    uiState: CollectionUiState,
    onFilterClick: (CollectionFilter) -> Unit,
    onSortClick: () -> Unit,
    onSteamClick: () -> Unit,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        CollectionTopBar(
            onSteamClick = onSteamClick
        )

        CollectionFilterRow(
            selectedFilter = uiState.selectedFilter,
            onFilterClick = onFilterClick
        )

        CollectionInfoRow(
            gameCount = uiState.games.size,
            selectedSort = uiState.selectedSort,
            onSortClick = onSortClick
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading collection...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            uiState.games.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No games in this section",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 24.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.games,
                        key = { game -> game.id }
                    ) { game ->
                        CollectionGameCard(
                            game = game,
                            onClick = {
                                onGameClick(game.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun CollectionTopBar(
    onSteamClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Collection",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        Surface(
            onClick = onSteamClick,
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary
            ),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    contentDescription = "Steam",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.steam_svgrepo_com),
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = "Steam",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
@Composable
private fun CollectionFilterRow(
    selectedFilter: CollectionFilter,
    onFilterClick: (CollectionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = CollectionFilter.entries.size
        ) { index ->

            val filter =
                CollectionFilter.entries[index]

            val selected =
                filter == selectedFilter

            AssistChip(
                onClick = {
                    onFilterClick(filter)
                },
                label = {
                    Text(
                        text = filter.title
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = when (filter) {
                            CollectionFilter.ALL ->
                                Icons.Default.GridView

                            CollectionFilter.PLANNED ->
                                Icons.Outlined.BookmarkBorder

                            CollectionFilter.PLAYING ->
                                Icons.Default.PlayArrow

                            CollectionFilter.COMPLETED ->
                                Icons.Outlined.CheckCircle

                            CollectionFilter.DROPPED ->
                                Icons.Outlined.Cancel
                        },
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor =
                        if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },

                    labelColor =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },

                    leadingIconContentColor =
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor =
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                                .copy(alpha = 0.45f)
                        }
                )
            )
        }
    }
}
@Composable
private fun CollectionInfoRow(
    gameCount: Int,
    selectedSort: CollectionSort,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onSortClick,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
                    .copy(alpha = 0.45f)
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = selectedSort.title,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(
                    modifier = Modifier.width(2.dp)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (gameCount == 1) {
                "1 game"
            } else {
                "$gameCount games"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
private fun CollectionGameCard(
    game: GameEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
                .copy(alpha = 0.25f)
        ),
        shadowElevation = 2.dp
    ) {
        Column {

            AsyncImage(
                model = game.imageUrl,
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.08f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = buildGameSubtitle(game),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (game.userRating != null) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        Text(
                            text = "${game.userRating}/10",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )
                        Text(
                            text = "No rated",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    CollectionStatusBadge(
                        status = game.status
                    )
                }
            }
        }
    }
}
private fun buildGameSubtitle(
    game: GameEntity
): String {
    val genre =
        game.genres.firstOrNull()?.name

    val platform =
        game.platforms.firstOrNull()

    return listOfNotNull(
        genre,
        platform
    ).joinToString(" • ")
}
@Composable
private fun CollectionStatusBadge(
    status: GameStatus,
    modifier: Modifier = Modifier
) {
    val text = when (status) {
        GameStatus.PLANNED -> "Plan"
        GameStatus.PLAYING -> "Playing"
        GameStatus.COMPLETED -> "Completed"
        GameStatus.DROPPED -> "Dropped"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 3.dp
            )
        )
    }
}
@Preview(
    name = "Collection Dark",
    showBackground = true,
    backgroundColor = 0xFF0B0F1A,
    widthDp = 390,
    heightDp = 850
)
@Composable
private fun CollectionDarkPreview() {
    GameStTheme(
        darkTheme = true
    ) {
        CollectionScreen(
            uiState = previewCollectionState,
            onFilterClick = {},
            onSortClick = {},
            onSteamClick = {},
            onGameClick = {}
        )
    }
}
@Preview(
    name = "Collection Light",
    showBackground = true,
    widthDp = 390,
    heightDp = 850
)
@Composable
private fun CollectionLightPreview() {
    GameStTheme(
        darkTheme = false
    ) {
        CollectionScreen(
            uiState = previewCollectionState,
            onFilterClick = {},
            onSortClick = {},
            onSteamClick = {},
            onGameClick = {}
        )
    }
}
private val previewCollectionState =
    CollectionUiState(
        games = listOf(
            previewCollectionGame(
                id = 1,
                title = "The Witcher 3: Wild Hunt",
                rating = 10,
                status = GameStatus.COMPLETED,
                genre = "RPG",
                platform = "PC"
            ),
            previewCollectionGame(
                id = 2,
                title = "Red Dead Redemption 2",
                rating = 9,
                status = GameStatus.COMPLETED,
                genre = "Action",
                platform = "PC"
            ),
            previewCollectionGame(
                id = 3,
                title = "Hades",
                rating = null,
                status = GameStatus.PLAYING,
                genre = "Roguelike",
                platform = "PC"
            ),
            previewCollectionGame(
                id = 4,
                title = "Elden Ring",
                rating = 9,
                status = GameStatus.PLAYING,
                genre = "Action RPG",
                platform = "PC"
            )
        ),
        selectedFilter = CollectionFilter.ALL,
        selectedSort = CollectionSort.RECENTLY_ADDED,
        isLoading = false
    )
private fun previewCollectionGame(
    id: Int,
    title: String,
    rating: Int?,
    status: GameStatus,
    genre: String,
    platform: String
): GameEntity {
    return GameEntity(
        id = id,
        title = title,
        imageUrl = "",
        description = "",
        releaseDate = "2023-01-01",
        ratingRawg = 4.5,
        metacritic = 90,
        playtime = 20,

        genres = listOf(
            StoredTag(
                id = id,
                name = genre,
                slug = genre.lowercase()
            )
        ),

        platforms = listOf(platform),

        developers = listOf(
            StoredCompany(
                id = id,
                name = "Developer",
                slug = "developer"
            )
        ),

        publishers = listOf(
            StoredCompany(
                id = id,
                name = "Publisher",
                slug = "publisher"
            )
        ),

        screenshots = emptyList(),

        ageRating = StoredAgeRating(
            id = 4,
            name = "Mature",
            slug = "mature"
        ),

        userRating = rating,
        status = status,
        hoursPlayed = 40
    )
}