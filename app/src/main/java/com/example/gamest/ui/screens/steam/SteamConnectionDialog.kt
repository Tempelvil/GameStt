package com.example.gamest.ui.screens.steam

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gamest.R
import com.example.gamest.ui.theme.GameStTheme
import java.util.Locale

@Composable
fun SteamConnectionDialog(
    uiState: SteamConnectionUiState,
    onProfileUrlChange: (String) -> Unit,
    onCheckConnection: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {
            if (!uiState.isLoading) {
                onDismissRequest()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !uiState.isLoading,
            dismissOnClickOutside = !uiState.isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
                    .copy(alpha = 0.35f)
            ),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                SteamDialogHeader(
                    closeEnabled = !uiState.isLoading,
                    onCloseClick = onDismissRequest
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Paste the link from your Steam profile. Your Game details must be public.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.profileUrl,
                    onValueChange = onProfileUrlChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    label = {
                        Text("Steam profile link")
                    },
                    placeholder = {
                        Text("https://steamcommunity.com/id/username/")
                    },
                    supportingText = {
                        Text("Only steamcommunity.com profile links are accepted")
                    },
                    isError = uiState.errorMessage != null,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCheckConnection,
                    enabled = !uiState.isLoading &&
                            uiState.profileUrl.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        if (uiState.result == null) {
                            "Check connection"
                        } else {
                            "Refresh"
                        }
                    )
                }

                uiState.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    SteamErrorContent(message)
                }

                uiState.result?.let { result ->
                    Spacer(modifier = Modifier.height(18.dp))
                    SteamConnectionResultContent(result)
                }
            }
        }
    }
}

@Composable
private fun SteamDialogHeader(
    closeEnabled: Boolean,
    onCloseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                painter = painterResource(R.drawable.steam_svgrepo_com),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(9.dp)
                    .size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Connect Steam",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Check library and playtime access",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onCloseClick,
            enabled = closeEnabled
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close"
            )
        }
    }
}

@Composable
private fun SteamErrorContent(message: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SteamConnectionResultContent(
    result: SteamConnectionResultUiModel
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Steam connected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SteamResultMetric(
                title = "Games",
                value = result.ownedGamesCount.toString(),
                modifier = Modifier.weight(1f)
            )

            SteamResultMetric(
                title = "Total playtime",
                value = result.totalPlaytimeMinutes.formatPlaytime(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SteamResultMetric(
            title = "Recently played",
            value = result.recentlyPlayedCount.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (result.recentlyPlayedCount > 0) {
                "Recent activity"
            } else {
                "Most played games"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (result.games.isEmpty()) {
            Text(
                text = "Steam returned no games for this profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 14.dp)
            )
        } else {
            result.games.forEachIndexed { index, game ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline
                            .copy(alpha = 0.20f)
                    )
                }

                SteamGameResultRow(game)
            }
        }
    }
}

@Composable
private fun SteamResultMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SteamGameResultRow(
    game: SteamGameUiModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(9.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = game.name
                        .firstOrNull()
                        ?.uppercase()
                        ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = game.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${game.totalPlaytimeMinutes.formatPlaytime()} total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        game.recentPlaytimeMinutes?.let { minutes ->
            Text(
                text = "${minutes.formatPlaytime()} / 2 weeks",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun Long.formatPlaytime(): String {
    if (this < 60) {
        return "${this}m"
    }

    val hours = this / 60.0
    return if (hours >= 100 || this % 60L == 0L) {
        "${hours.toLong()}h"
    } else {
        String.format(Locale.US, "%.1fh", hours)
    }
}

@Preview(
    name = "Steam connection",
    showBackground = true,
    widthDp = 390,
    heightDp = 850
)
@Composable
private fun SteamConnectionDialogPreview() {
    GameStTheme(darkTheme = true) {
        SteamConnectionDialog(
            uiState = previewSteamConnectionState,
            onProfileUrlChange = {},
            onCheckConnection = {},
            onDismissRequest = {}
        )
    }
}

private val previewSteamConnectionState = SteamConnectionUiState(
    profileUrl = "https://steamcommunity.com/id/username/",
    result = SteamConnectionResultUiModel(
        steamId = "76561190000000000",
        ownedGamesCount = 124,
        totalPlaytimeMinutes = 147_600,
        recentlyPlayedCount = 3,
        games = listOf(
            SteamGameUiModel(
                appId = 1,
                name = "Elden Ring",
                totalPlaytimeMinutes = 6_720,
                recentPlaytimeMinutes = 744
            ),
            SteamGameUiModel(
                appId = 2,
                name = "Baldur's Gate 3",
                totalPlaytimeMinutes = 5_160,
                recentPlaytimeMinutes = 248
            ),
            SteamGameUiModel(
                appId = 3,
                name = "Cyberpunk 2077",
                totalPlaytimeMinutes = 3_720,
                recentPlaytimeMinutes = 96
            )
        )
    )
)
