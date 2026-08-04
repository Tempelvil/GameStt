package com.example.gamest.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items as lazyColumnItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gamest.model.GameFilter
import com.example.gamest.model.GameTagUiModel
import com.example.gamest.model.GameUiModel
import com.example.gamest.ui.theme.GameStTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip


@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onSearchQueryChange: (String) -> Unit,
    onSaveGameClick: (Int) -> Unit,
    onGenreClick: (GameFilter) -> Unit,
    onMoreGenreClick:()->Unit,
    onGenreSelected: (GameTagUiModel) -> Unit,
    onRetryGenresClick: () -> Unit,
    onLoadNextPage:()->Unit,
    onGameClick:(Int)->Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    var showGenresSheet by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(gridState, uiState.games.size) {
        snapshotFlow {
            gridState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.collect { lastVisibleIndex ->
            val totalItemsCount = gridState.layoutInfo.totalItemsCount

            if (
                lastVisibleIndex != null &&
                totalItemsCount > 0 &&
                lastVisibleIndex >= totalItemsCount - 4
            ) {
                onLoadNextPage()
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ){
        item(span = { GridItemSpan(maxLineSpan) }) {
            SearchTextField(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            val sectionTitle = when {
                uiState.searchQuery.isNotBlank() -> "Search Results"
                else -> uiState.selectedFilter.title
            }

            val sectionDescription = when {
                uiState.searchQuery.isNotBlank() -> "Games matching your search"
                uiState.selectedFilter == GameFilter.TopRated -> "The highest rated games by the community"
                else -> "Popular ${uiState.selectedFilter.title} games"
            }

            Column {
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = sectionDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            GenreChipsRow(
                selectedGenre = uiState.selectedFilter,
                onGenreClick = onGenreClick,
                onMoreGenreClick = {
                    showGenresSheet = true
                    onMoreGenreClick()
                }
            )
        }
        if (uiState.isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("Loading...")
            }
        }

        uiState.errorMessage?.let { message ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        items(uiState.games) { game ->
            GameCard(
                game = game,
                onClick = {
                    onGameClick(game.id)
                },
                onSaveClick = { onSaveGameClick(game.id) }
            )
        }
        if (uiState.isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }


    }
    if (showGenresSheet) {
        GenresBottomSheet(
            genres = uiState.availableGenres,
            isLoading = uiState.isGenresLoading,
            errorMessage = uiState.genresErrorMessage,
            onGenreClick = { genre ->
                showGenresSheet = false
                onGenreSelected(genre)
            },
            onRetryClick = onRetryGenresClick,
            onDismissRequest = {
                showGenresSheet = false
            }
        )
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenresBottomSheet(
    genres: List<GameTagUiModel>,
    isLoading: Boolean,
    errorMessage: String?,
    onGenreClick: (GameTagUiModel) -> Unit,
    onRetryClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = 220.dp,
                    max = 600.dp
                )
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                        .copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Text(
                text = "All genres",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                )
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline
                    .copy(alpha = 0.35f)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onRetryClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Try again")
                        }
                    }
                }

                genres.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No genres available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        lazyColumnItems(
                            items = genres,
                            key = { genre -> genre.id }
                        ) { genre ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onGenreClick(genre)
                                    }
                                    .padding(
                                        horizontal = 20.dp,
                                        vertical = 16.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = genre.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled
                                        .KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme
                                        .onSurfaceVariant
                                )
                            }

                            if (genre != genres.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 20.dp),
                                    color = MaterialTheme.colorScheme.outline
                                        .copy(alpha = 0.25f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun GameCard(
    game: GameUiModel,
    onClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = game.imageUrl,
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = game.metacritic?.toString() ?: "—",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${game.genres.firstOrNull() ?: "Unknown"} • ${game.platforms.firstOrNull() ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (game.isSaved) {
                                Icons.Default.Bookmark
                            } else {
                                Icons.Default.BookmarkBorder
                            },
                            contentDescription = "Save game",
                            tint = if (game.isSaved) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text("Search games...")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp)
    )
}
@Composable
fun GenreChipsRow(
    selectedGenre: GameFilter,
    onMoreGenreClick: () -> Unit,
    onGenreClick: (GameFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            GameFilter.TopRated,
            GameFilter.Rpg,
            GameFilter.Action,
            GameFilter.Pc
        ).forEach { filter ->
            FilterChip(
                selected = selectedGenre == filter,
                onClick = { onGenreClick(filter) },
                label = { Text(filter.title) },
                leadingIcon = if (selectedGenre == filter) {
                    {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    null
                }
            )
        }

        AssistChip(
            onClick = onMoreGenreClick,
            label = {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "More filters"
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    GameStTheme {
        SearchScreen(
            uiState = SearchUiState(
                games = listOf(
                    GameUiModel(
                        id = 1,
                        title = "The Witcher 3",
                        imageUrl = "",
                        rating = 4.8,
                        genres = listOf("RPG", "Adventure"),
                        platforms = listOf("PC", "PlayStation")
                    ),
                    GameUiModel(
                        id = 2,
                        title = "Cyberpunk 2077",
                        imageUrl = "",
                        rating = 4.5,
                        genres = listOf("RPG", "Action"),
                        platforms = listOf("PC", "Xbox")
                    )
                ),
                availableGenres = listOf(
                    GameTagUiModel(
                        id = 1,
                        name = "Action",
                        slug = "action"
                    ),
                    GameTagUiModel(
                        id = 2,
                        name = "RPG",
                        slug = "role-playing-games-rpg"
                    ),
                    GameTagUiModel(
                        id = 3,
                        name = "Adventure",
                        slug = "adventure"
                    ),
                    GameTagUiModel(
                        id = 4,
                        name = "Strategy",
                        slug = "strategy"
                    ),
                    GameTagUiModel(
                        id = 5,
                        name = "Shooter",
                        slug = "shooter"
                    ),
                    GameTagUiModel(
                        id = 6,
                        name = "Shooter",
                        slug = "shooter"
                    ),
                    GameTagUiModel(
                        id = 7,
                        name = "Shooter",
                        slug = "shooter"
                    ),
                    GameTagUiModel(
                        id = 8,
                        name = "Shooter",
                        slug = "shooter"
                    ),
                    GameTagUiModel(
                        id = 9,
                        name = "Shooter",
                        slug = "shooter"
                    ),
                    GameTagUiModel(
                        id = 10,
                        name = "Shooter",
                        slug = "shooter"
                    ),
                    GameTagUiModel(
                        id = 11,
                        name = "Shooter",
                        slug = "shooter"
                    )
                )
            ),
            onSearchQueryChange = {},
            onSaveGameClick = {},
            onGenreClick = {},
            onMoreGenreClick = {},
            onGenreSelected = {},
            onRetryGenresClick = {},
            onLoadNextPage = {},
            onGameClick = {},
            modifier = Modifier
        )
    }
}