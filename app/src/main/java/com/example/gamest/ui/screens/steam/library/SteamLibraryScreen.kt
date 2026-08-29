package com.example.gamest.ui.screens.steam.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gamest.R
import com.example.gamest.ui.theme.GameStTheme
import com.example.gamest.ui.components.CompactFilterBar
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import com.example.gamest.data.local.SteamProfileStatus

@Composable
fun SteamLibraryScreen(
    uiState: SteamLibraryUiState,
    onBackClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: (SteamLibraryFilter) -> Unit,
    onSortClick: (SteamLibrarySort) -> Unit,
    onSyncClick: () -> Unit,
    onGameClick: (Int) -> Unit,
    onAddProfileClick: () -> Unit,
    onActivateProfile: (SteamProfileUiModel) -> Unit,
    onPauseProfile: () -> Unit,
    onUnlinkProfile: (() -> Unit) -> Unit,
    onDeleteProfile: (SteamProfileUiModel, () -> Unit) -> Unit,
    onProfileDetached: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    var profilePendingDeletion by remember {
        mutableStateOf<SteamProfileUiModel?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        SteamLibraryHeader(
            onBackClick = onBackClick,
            onSettingsClick = { showSettings = true }
        )

        SteamProfileCard(
            uiState = uiState,
            onSyncClick = onSyncClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        SteamLibrarySearchField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            selectedSort = uiState.selectedSort,
            onSortClick = onSortClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        SteamLibraryControls(
            selectedFilter = uiState.selectedFilter,
            onFilterClick = onFilterClick
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        when {
            uiState.totalGamesCount == 0 && uiState.isSyncing -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.games.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No games match these filters",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.games,
                        key = SteamLibraryGameUiModel::appId
                    ) { game ->
                        SteamLibraryGameCard(
                            game = game,
                            isOpening = uiState.openingGameAppId == game.appId,
                            enabled = uiState.openingGameAppId == null,
                            onClick = { onGameClick(game.appId) }
                        )
                    }
                }
            }
        }
    }

    if (showSettings) {
        SteamProfilesSettingsSheet(
            uiState = uiState,
            onDismissRequest = { showSettings = false },
            onAddProfileClick = onAddProfileClick,
            onActivateProfile = onActivateProfile,
            onPauseProfile = onPauseProfile,
            onUnlinkProfile = {
                onUnlinkProfile {
                    showSettings = false
                    onProfileDetached()
                }
            },
            onDeleteProfile = { profile ->
                profilePendingDeletion = profile
            }
        )
    }

    profilePendingDeletion?.let { profile ->
        AlertDialog(
            onDismissRequest = { profilePendingDeletion = null },
            title = { Text("Delete Steam data?") },
            text = {
                Text(
                    "The library and all playtime history for " +
                        "${profile.personaName} will be permanently deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val wasActive = profile.steamId == uiState.activeSteamId
                        onDeleteProfile(profile) {
                            profilePendingDeletion = null
                            if (wasActive) {
                                showSettings = false
                                onProfileDetached()
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { profilePendingDeletion = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SteamLibraryHeader(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(
            text = "Steam Library",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Default.Settings, contentDescription = "Steam settings")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SteamProfilesSettingsSheet(
    uiState: SteamLibraryUiState,
    onDismissRequest: () -> Unit,
    onAddProfileClick: () -> Unit,
    onActivateProfile: (SteamProfileUiModel) -> Unit,
    onPauseProfile: () -> Unit,
    onUnlinkProfile: () -> Unit,
    onDeleteProfile: (SteamProfileUiModel) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Steam profiles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Unlinking preserves the library and playtime history.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            uiState.profiles.forEach { profile ->
                val active = profile.steamId == uiState.activeSteamId
                Surface(
                    onClick = {
                        if (!active) onActivateProfile(profile)
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = if (active) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = profile.avatarUrl,
                            contentDescription = null,
                            placeholder = painterResource(R.drawable.steam_svgrepo_com),
                            error = painterResource(R.drawable.steam_svgrepo_com),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                profile.personaName,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when (profile.status) {
                                    SteamProfileStatus.PAUSED -> "Paused"
                                    SteamProfileStatus.UNLINKED -> "Unlinked • history saved"
                                    else -> if (active) "Active" else "Linked"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDeleteProfile(profile) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete profile data",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (uiState.profiles.size < 4) {
                Button(
                    onClick = onAddProfileClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Steam profile")
                }
            } else {
                Text(
                    text = "Maximum of 4 saved profiles reached",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.activeSteamId != null) {
                if (uiState.activeProfileStatus == SteamProfileStatus.PAUSED) {
                    TextButton(
                        onClick = {
                            uiState.profiles
                                .firstOrNull { it.steamId == uiState.activeSteamId }
                                ?.let(onActivateProfile)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resume synchronization")
                    }
                } else {
                    TextButton(
                        onClick = onPauseProfile,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause synchronization")
                    }
                }

                TextButton(
                    onClick = onUnlinkProfile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LinkOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unlink profile")
                }
            }
        }
    }
}

@Composable
private fun SteamProfileCard(
    uiState: SteamLibraryUiState,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = uiState.avatarUrl,
                contentDescription = null,
                placeholder = painterResource(R.drawable.steam_svgrepo_com),
                error = painterResource(R.drawable.steam_svgrepo_com),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(52.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.personaName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${uiState.totalGamesCount} games",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                uiState.lastSyncAt?.let { timestamp ->
                    Text(
                        text = "Synced ${timestamp.formatDateTime()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onSyncClick,
                enabled = !uiState.isSyncing &&
                    uiState.activeProfileStatus == SteamProfileStatus.LINKED
            ) {
                if (uiState.isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Sync")
                }
            }
        }
    }
}

@Composable
private fun SteamLibraryControls(
    selectedFilter: SteamLibraryFilter,
    onFilterClick: (SteamLibraryFilter) -> Unit
) {
    CompactFilterBar(
        items = SteamLibraryFilter.entries,
        selectedItem = selectedFilter,
        onItemClick = onFilterClick,
        label = { filter ->
            if (filter == SteamLibraryFilter.NEVER_PLAYED) {
                "Unplayed"
            } else {
                filter.title
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun SteamLibrarySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    selectedSort: SteamLibrarySort,
    onSortClick: (SteamLibrarySort) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text("Search Steam library...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort by ${selectedSort.title}"
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SteamLibrarySort.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(sort.title) },
                            onClick = {
                                onSortClick(sort)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        },
        supportingText = {
            Text("Sorted by ${selectedSort.title.lowercase()}")
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun SteamLibraryGameCard(
    game: SteamLibraryGameUiModel,
    isOpening: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.imageUrl,
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.steam_svgrepo_com),
                error = painterResource(R.drawable.steam_svgrepo_com),
                modifier = Modifier
                    .width(132.dp)
                    .height(70.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${game.totalPlaytimeMinutes.formatPlaytime()} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                game.recentPlaytimeMinutes
                    ?.takeIf { minutes -> minutes > 0 }
                    ?.let { minutes ->
                        Text(
                            text = "${minutes.formatPlaytime()} in 2 weeks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
            }

            if (isOpening) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

private fun Long.formatPlaytime(): String {
    if (this < 60) return "${this}m"
    val hours = this / 60.0
    return if (this % 60L == 0L || hours >= 100) {
        "${hours.toLong()}h"
    } else {
        String.format(Locale.US, "%.1fh", hours)
    }
}

private fun Long.formatDateTime(): String {
    return DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT
    ).format(Date(this))
}

@Preview(showBackground = true, widthDp = 390, heightDp = 850)
@Composable
private fun SteamLibraryScreenPreview() {
    GameStTheme(darkTheme = true) {
        SteamLibraryScreen(
            uiState = SteamLibraryUiState(
                personaName = "Steam player",
                totalGamesCount = 156,
                games = listOf(
                    SteamLibraryGameUiModel(
                        appId = 108600,
                        name = "Project Zomboid",
                        imageUrl = "",
                        totalPlaytimeMinutes = 21_120,
                        recentPlaytimeMinutes = 900,
                        lastPlayedAt = null
                    )
                )
            ),
            onBackClick = {},
            onSearchQueryChange = {},
            onFilterClick = {},
            onSortClick = {},
            onSyncClick = {},
            onGameClick = {},
            onAddProfileClick = {},
            onActivateProfile = {},
            onPauseProfile = {},
            onUnlinkProfile = {},
            onDeleteProfile = { _, _ -> },
            onProfileDetached = {}
        )
    }
}
