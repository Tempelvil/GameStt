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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.gamest.ui.components.PlatformIcons
import com.example.gamest.ui.components.CompactFilterBar
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CollectionScreen(
    uiState: CollectionUiState,
    onFilterClick: (CollectionFilter) -> Unit,
    onSortClick: (CollectionSort) -> Unit,
    onSteamClick: () -> Unit,
    onOpenGame: (Int) -> Unit,
    onEditConfirm: (Int,GameStatus,Int?,Int) -> Unit,
    onDeleteGame: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGameId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    val selectedGame = uiState.games
        .firstOrNull { game ->
            game.id == selectedGameId
        }
    var editingGameId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    val editingGame = uiState.games
        .firstOrNull { game ->
            game.id == editingGameId
        }
    var deletingGameId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    val deletingGame = uiState.games.firstOrNull(){
        game ->
        game.id ==deletingGameId
    }

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
                            isSelected = game.id == selectedGameId,
                            onClick = {
                                selectedGameId = game.id
                            }
                        )
                    }
                }
            }
        }
    }
    if (selectedGame != null) {

        CollectionGameActionsBottomSheet(
            game = selectedGame,

            onDismissRequest = {
                selectedGameId = null
            },

            onOpenClick = {
                val gameId = selectedGame.id

                selectedGameId = null
                onOpenGame(gameId)
            },

            onEditClick = {
                editingGameId = selectedGame.id
                selectedGameId = null
            },

            onDeleteClick = {
                deletingGameId = selectedGame.id
                selectedGameId = null
            }
        )
    }
    if (editingGame != null) {
        EditGameDialog(
            game = editingGame,

            onDismissRequest = {
                editingGameId = null
            },

            onConfirm = { status, rating, hours ->
                val gameId = editingGame.id

                editingGameId = null

                onEditConfirm(
                    gameId,
                    status,
                    rating,
                    hours
                )
            }
        )
    }
    if (deletingGame != null) {
        DeleteGameDialog(
            game = deletingGame,

            onDismissRequest = {
                deletingGameId = null
            },

            onConfirm = {
                val gameId = deletingGame.id

                deletingGameId = null
                onDeleteGame(gameId)
            }
        )
    }
}

@Composable
private fun EditGameDialog(
    game: GameEntity,
    onDismissRequest: () -> Unit,
    onConfirm: (
        GameStatus,
        Int?,
        Int
    ) -> Unit
) {
    var selectedStatus by rememberSaveable(game.id) {
        mutableStateOf(game.status)
    }

    var ratingText by rememberSaveable(game.id) {
        mutableStateOf(
            game.userRating?.toString().orEmpty()
        )
    }

    var hoursText by rememberSaveable(game.id) {
        mutableStateOf(
            game.hoursPlayed.toString()
        )
    }

    val userRating = ratingText.toIntOrNull()
    val hoursPlayed = hoursText.toIntOrNull() ?: 0

    val isRatingValid =
        ratingText.isBlank() ||
                userRating in 1..10

    val isHoursValid =
        hoursText.isBlank() ||
                hoursPlayed >= 0

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(
                    RoundedCornerShape(24.dp)
                )
                .background(
                    MaterialTheme.colorScheme.surface
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                        .copy(alpha = 0.35f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {

            Text(
                text = "Редактировать игру",
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = game.title,
                style =
                    MaterialTheme.typography.titleMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color =
                            MaterialTheme.colorScheme.outline
                                .copy(alpha = 0.55f),
                        shape =
                            RoundedCornerShape(12.dp)
                    )
            ) {
                GameStatus.entries.forEachIndexed {
                        index,
                        status ->

                    val selected =
                        selectedStatus == status

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable {
                                selectedStatus = status
                            },
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text = CollectionFilter.entries
                                .first { it.status == status }
                                .title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1
                        )
                    }

                    if (
                        index !=
                        GameStatus.entries.lastIndex
                    ) {
                        VerticalDivider(
                            modifier =
                                Modifier.height(26.dp),
                            color =
                                MaterialTheme.colorScheme.outline
                                    .copy(alpha = 0.4f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ratingText,

                onValueChange = { value ->
                    if (
                        value.isBlank() ||
                        value.all(Char::isDigit)
                    ) {
                        ratingText =
                            value.take(2)
                    }
                },

                label = {
                    Text("Your rating")
                },

                placeholder = {
                    Text("1–10")
                },

                isError = !isRatingValid,

                singleLine = true,

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hoursText,

                onValueChange = { value ->
                    if (
                        value.isBlank() ||
                        value.all(Char::isDigit)
                    ) {
                        hoursText = value
                    }
                },

                label = {
                    Text("Hours played")
                },

                isError = !isHoursValid,

                singleLine = true,

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),

                modifier =
                    Modifier.fillMaxWidth()
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {
                TextButton(
                    onClick =
                        onDismissRequest
                ) {
                    Text("Cancel")
                }

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Button(
                    onClick = {
                        onConfirm(
                            selectedStatus,
                            userRating,
                            hoursPlayed
                        )
                    },
                    enabled =
                        isRatingValid &&
                                isHoursValid
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionGameActionsBottomSheet(
    game: GameEntity,
    onDismissRequest: () -> Unit,
    onOpenClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                )
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = buildString {
                        append(
                            CollectionFilter.entries
                                .first { it.status == game.status }
                                .title
                        )

                        game.userRating?.let { rating ->
                            append(" • $rating/10")
                        }

                        if (game.hoursPlayed > 0) {
                            append(" • ${game.hoursPlayed} ч")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            ListItem(
                headlineContent = {
                    Text("Open")
                },
                supportingContent = {
                    Text("Open game details")
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable {
                    onOpenClick()
                }
            )

            ListItem(
                headlineContent = {
                    Text("Edit")
                },
                supportingContent = {
                    Text("Edit status, rating and hours")
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable {
                    onEditClick()
                }
            )

            ListItem(
                headlineContent = {
                    Text(
                        text = "Delete from Collection",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                supportingContent = {
                    Text("Remove saved game data")
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable {
                    onDeleteClick()
                }
            )
        }
    }
}
@Composable
private fun DeleteGameDialog(
    game: GameEntity,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,

        title = {
            Text("Remove game?")
        },

        text = {
            Text(
                text = "Remove \"${game.title}\" from your collection?"
            )
        },

        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "Remove",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
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
    CompactFilterBar(
        items = CollectionFilter.entries,
        selectedItem = selectedFilter,
        onItemClick = onFilterClick,
        label = CollectionFilter::title,
        icon = { filter ->
            when (filter) {
                CollectionFilter.ALL -> Icons.Default.GridView
                CollectionFilter.PLANNED -> Icons.Outlined.BookmarkBorder
                CollectionFilter.PLAYING -> Icons.Default.PlayArrow
                CollectionFilter.COMPLETED -> Icons.Outlined.CheckCircle
                CollectionFilter.DROPPED -> Icons.Outlined.Cancel
            }
        },
        modifier = modifier.padding(horizontal = 16.dp)
    )
}
@Composable
private fun CollectionInfoRow(
    gameCount: Int,
    selectedSort: CollectionSort,
    onSortClick: (CollectionSort) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

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
            onClick = {expanded = true},
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
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            CollectionSort.entries.forEach { sort ->

                DropdownMenuItem(
                    text = {
                        Text(sort.title)
                    },

                    onClick = {
                        onSortClick(sort)
                        expanded = false
                    }
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
    isSelected: Boolean,
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
            width = if (isSelected) {
                2.dp
            } else {
                1.dp
            },
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
                    .copy(alpha = 0.25f)
            }
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.genres.firstOrNull()?.name ?: "Unknown",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (game.platforms.isNotEmpty()) {
                        PlatformIcons(
                            platforms = game.platforms,
                            iconSize = 13.dp,
                            showLeadingSeparator = true
                        )
                    }
                }

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
                            text = "Not rated",
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
            onOpenGame = {},
            onEditConfirm = { _, _, _, _ -> },
            onDeleteGame = {},
            modifier = Modifier,
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
            onOpenGame = {},
            onEditConfirm = { _, _, _, _ -> },
            onDeleteGame = {},
            modifier = Modifier,
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
