package com.example.gamest.ui.screens.details

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.example.gamest.model.GameCompanyUiModel
import com.example.gamest.model.GameDetailsUiModel
import com.example.gamest.model.GameTagUiModel
import com.example.gamest.ui.theme.GameStTheme
import kotlin.math.absoluteValue
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.gamest.R
import com.example.gamest.model.GameAgeRatingUiModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gamest.data.local.GameStatus

@Composable
fun DetailsScreen(
    uiState: GameDetailsUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSaveConfirm: (
        status: GameStatus,
        userRating: Int?,
        hoursPlayed: Int
    ) -> Unit,
    onDeleteConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    onDeveloperClick: (GameCompanyUiModel) -> Unit,
    onAgeRatingClick: (GameAgeRatingUiModel) -> Unit,
    onPublisherClick: (GameCompanyUiModel) -> Unit,
) {
    var showSaveDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    when (uiState) {
        GameDetailsUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        }

        is GameDetailsUiState.Error -> {
            DetailsErrorScreen(
                message = uiState.message,
                onBackClick = onBackClick,
                onRetryClick = onRetryClick,
                modifier = modifier
            )
        }

        is GameDetailsUiState.Success -> {
            val game = uiState.game

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.padding(
                                start = 8.dp,
                                top = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Text(
                            text = "GameShelf",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 4.dp)
                        )
                    }

                }

                item {
                    GameScreenshotPager(
                        screenshots = game.screenshots,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                item {
                    GameDetailsHeader(
                        game = game,
                        onSaveClick = {
                            if (game.isSaved) {
                                showDeleteDialog = true
                            } else {
                                showSaveDialog = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                item {
                    GameGenreChips(
                        genres = game.genres,
                        onGenreClick = { genre ->
                            // позже навигация в поиск
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                item {
                    GameAboutSection(
                        description = game.description,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                item {
                    GameAdditionalInfo(
                        developers = game.developers,
                        publishers = game.publishers,
                        ageRating = game.ageRating,
                        playtime = game.playtime,
                        onDeveloperClick = onDeveloperClick,
                        onPublisherClick = onPublisherClick,
                        onAgeRatingClick = onAgeRatingClick,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }


            }
            if (showSaveDialog) {
                SaveGameDialog(
                    defaultHours = game.playtime,
                    onDismissRequest = {
                        showSaveDialog = false
                    },
                    onConfirm = { status, userRating, hoursPlayed ->
                        showSaveDialog = false

                        onSaveConfirm(
                            status,
                            userRating,
                            hoursPlayed
                        )
                    }
                )
            }

            if (showDeleteDialog) {
                DeleteGameConfirmationDialog(
                    gameTitle = game.title,
                    onDismissRequest = {
                        showDeleteDialog = false
                    },
                    onConfirm = {
                        showDeleteDialog = false
                        onDeleteConfirm()
                    }
                )
            }
        }
    }
}
@Composable
private fun SaveGameDialog(
    defaultHours: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (
        status: GameStatus,
        userRating: Int?,
        hoursPlayed: Int
    ) -> Unit
) {
    var selectedStatus by rememberSaveable {
        mutableStateOf(GameStatus.PLANNED)
    }

    var ratingText by rememberSaveable {
        mutableStateOf("")
    }

    var hoursText by rememberSaveable(defaultHours) {
        mutableStateOf(
            defaultHours
                .takeIf { it > 0 }
                ?.toString()
                .orEmpty()
        )
    }

    val userRating = ratingText.toIntOrNull()
    val hoursPlayed = hoursText.toIntOrNull() ?: 0

    val isRatingValid =
        ratingText.isBlank() || userRating in 1..10

    val isHoursValid =
        hoursText.isBlank() || hoursPlayed >= 0

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
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add to collection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Choose a section",
                style = MaterialTheme.typography.titleSmall
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameStatus.entries.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = {
                            selectedStatus = status
                        },
                        label = {
                            Text(status.toDisplayName())
                        }
                    )
                }
            }

            OutlinedTextField(
                value = ratingText,
                onValueChange = { newValue ->
                    if (
                        newValue.isBlank() ||
                        newValue.all(Char::isDigit)
                    ) {
                        ratingText = newValue.take(2)
                    }
                },
                label = {
                    Text("Your rating")
                },
                supportingText = {
                    Text("Optional, from 1 to 10")
                },
                isError = !isRatingValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hoursText,
                onValueChange = { newValue ->
                    if (
                        newValue.isBlank() ||
                        newValue.all(Char::isDigit)
                    ) {
                        hoursText = newValue
                    }
                },
                label = {
                    Text("Hours played")
                },
                supportingText = {
                    if (defaultHours > 0) {
                        Text(
                            "RAWG suggested value: $defaultHours h"
                        )
                    } else {
                        Text("Enter 0 if you haven't played yet")
                    }
                },
                isError = !isHoursValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onConfirm(
                            selectedStatus,
                            userRating,
                            hoursPlayed
                        )
                    },
                    enabled = isRatingValid && isHoursValid
                ) {
                    Text("Save")
                }
            }
        }
    }
}

private fun GameStatus.toDisplayName(): String {
    return when (this) {
        GameStatus.PLANNED -> "Planned"
        GameStatus.PLAYING -> "Playing"
        GameStatus.COMPLETED -> "Completed"
        GameStatus.DROPPED -> "Dropped"
        else -> ""
    }
}

@Composable
private fun DeleteGameConfirmationDialog(
    gameTitle: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Remove from collection?")
        },
        text = {
            Text(
                text = "Are you sure you want to remove \"$gameTitle\" from your collection? Your rating, status and played hours will be deleted."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Remove")
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
private fun DetailsErrorScreen(
    message: String,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "GameShelf",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Couldn't load game",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

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

            OutlinedButton(
                onClick = onBackClick
            ) {
                Text("Back to search")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameScreenshotPager(
    screenshots: List<String>,
    modifier: Modifier = Modifier
) {
    if (screenshots.isEmpty()) return

    val pagerState = rememberPagerState(
        pageCount = { screenshots.size }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(235.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 0.dp,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            val pageOffset =
                (pagerState.currentPage - page) +
                        pagerState.currentPageOffsetFraction

            val scale = lerp(
                start = 0.92f,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                AsyncImage(
                    model = screenshots[page],
                    contentDescription = "Game screenshot ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(
            text = "${pagerState.currentPage + 1} / ${screenshots.size}",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 52.dp, top = 12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        val borderColor =
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    )
                )
                .background(MaterialTheme.colorScheme.background)
                .drawWithContent {
                    drawContent()

                    val strokeWidth = 2.dp.toPx()
                    val halfStroke = strokeWidth / 2
                    val radius = 18.dp.toPx()

                    val path = Path().apply {
                        moveTo(halfStroke, size.height)

                        lineTo(halfStroke, radius)

                        quadraticTo(
                            halfStroke,
                            halfStroke,
                            radius,
                            halfStroke
                        )

                        lineTo(size.width - radius, halfStroke)

                        quadraticTo(
                            size.width - halfStroke,
                            halfStroke,
                            size.width - halfStroke,
                            radius
                        )

                        lineTo(
                            size.width - halfStroke,
                            size.height
                        )
                    }

                    drawPath(
                        path = path,
                        color = borderColor,
                        style = Stroke(width = strokeWidth)
                    )
                }
                .padding(
                    horizontal = 14.dp,
                    vertical = 8.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(screenshots.size) { index ->
                val isSelected =
                    index == pagerState.currentPage

                Box(
                    modifier = Modifier
                        .size(
                            if (isSelected) 10.dp else 8.dp
                        )
                        .background(
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun GameDetailsHeader(
    game: GameDetailsUiModel,
    onSaveClick:()->Unit,
    modifier: Modifier= Modifier
){
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = game.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        GameMetadataRow(game = game)

        PlatformIconsRow(
            platforms = game.platforms
        )
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (game.isSaved) {
                    Icons.Default.Bookmark
                } else {
                    Icons.Default.BookmarkBorder
                },
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (game.isSaved) {
                    "Remove from collection"
                } else {
                    "Save to collection"
                }
            )
        }
    }
}

@Composable
fun GameMetadataRow(
    game: GameDetailsUiModel,
    modifier: Modifier= Modifier
){
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )

        Text(
            text = game.releaseDate,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        VerticalDivider(
            modifier = Modifier.height(26.dp)
        )
        if (game.metacritic != null) {
            Icon(
                painter = painterResource(R.drawable.metacritic),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = game.metacritic.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Metacritic",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (game.rating > 0.0) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = "%.1f".format(game.rating),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "RAWG",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
private data class PlatformIcon(
    @DrawableRes val iconRes: Int,
    val contentDescription: String
)
@Composable
private fun PlatformIconsRow(
    platforms: List<String>,
    modifier: Modifier = Modifier
) {
    val platformIcons = platforms
        .mapNotNull { platform ->
            when {
                platform.contains("PC", ignoreCase = true) -> {
                    PlatformIcon(
                        iconRes = R.drawable.logo_windows_svgrepo_com,
                        contentDescription = "PC"
                    )
                }

                platform.contains("Xbox", ignoreCase = true) -> {
                    PlatformIcon(
                        iconRes = R.drawable.xbox_svgrepo_com,
                        contentDescription = "Xbox"
                    )
                }

                platform.contains("PlayStation", ignoreCase = true) -> {
                    PlatformIcon(
                        iconRes = R.drawable.play_station_logo_svgrepo_com,
                        contentDescription = "PlayStation"
                    )
                }

                platform.contains("Nintendo", ignoreCase = true) -> {
                    PlatformIcon(
                        iconRes = R.drawable.nintendo_switch_svgrepo_com,
                        contentDescription = "Nintendo Switch"
                    )
                }

                platform.contains("Android", ignoreCase = true) -> {
                    PlatformIcon(
                        iconRes = R.drawable.android_svgrepo_com,
                        contentDescription = "Android"
                    )
                }
                platform.contains("Ios", ignoreCase = true) -> {
                    PlatformIcon(
                        iconRes = R.drawable.ios_svgrepo_com,
                        contentDescription = "Ios"
                    )
                }
                platform.contains("Linux", ignoreCase = true) -> {
                    PlatformIcon(
                        iconRes = R.drawable.linux_svgrepo_com,
                        contentDescription = "Linux"
                    )
                }

                else -> null
            }
        }
        .distinctBy { it.iconRes }

    if (platformIcons.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        platformIcons.forEach { platformIcon ->
            Icon(
                painter = painterResource(
                    id = platformIcon.iconRes
                ),
                contentDescription = platformIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
@Composable
fun GameGenreChips(
    genres: List<GameTagUiModel>,
    onGenreClick:(GameTagUiModel)->Unit,
    modifier:Modifier = Modifier
){

    if (genres.isEmpty()){
        return
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Genres",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            genres.forEach { genre ->
                AssistChip(
                    onClick = {
                        onGenreClick(genre)
                    },
                    label = {
                        Text(
                            text = genre.name
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.outline
                            .copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

@Composable
fun GameAboutSection(
    description: String,
    modifier: Modifier= Modifier
){
    if(description.isBlank()) return

    var isExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var hasTextOverflow by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (isExpanded) Int.MAX_VALUE else 6,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded) {
                    hasTextOverflow = textLayoutResult.hasVisualOverflow
                }
            }
        )

        if (hasTextOverflow || isExpanded) {
            TextButton(
                onClick = {
                    isExpanded = !isExpanded
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (isExpanded) {
                        "Show less"
                    } else {
                        "Read more"
                    }
                )
            }
        }

    }
}
private enum class CompanyDialogType {
    Developers,
    Publishers
}

@Composable
fun GameAdditionalInfo(
    developers: List<GameCompanyUiModel>,
    publishers: List<GameCompanyUiModel>,
    ageRating: GameAgeRatingUiModel?,
    playtime: Int,
    onDeveloperClick: (GameCompanyUiModel) -> Unit,
    onPublisherClick: (GameCompanyUiModel) -> Unit,
    onAgeRatingClick: (GameAgeRatingUiModel) -> Unit,
    modifier: Modifier = Modifier
){
    var openedDialog by rememberSaveable {
        mutableStateOf<CompanyDialogType?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        GameInfoRow(
            title = "Developer",
            value = developers
                .joinToString(separator = ", ") { developer ->
                    developer.name
                }
                .ifBlank { "No data" },
            icon = Icons.Default.Business,
            onClick = if (developers.isNotEmpty()) {
                {
                    openedDialog = CompanyDialogType.Developers
                }
            } else {
                null
            }
        )

        GameInfoDivider()

        GameInfoRow(
            title = "Publisher",
            value = publishers
                .joinToString(separator = ", ") { publisher ->
                    publisher.name
                }
                .ifBlank { "No data" },
            icon = Icons.Default.SportsEsports,
            onClick = if (publishers.isNotEmpty()) {
                {
                    openedDialog = CompanyDialogType.Publishers
                }
            } else {
                null
            }
        )

        GameInfoDivider()

        GameInfoRow(
            title = "Age rating",
            value = ageRating?.name ?: "No data",
            icon = Icons.Default.Security,
            onClick = ageRating?.let { rating ->
                {
                    onAgeRatingClick(rating)
                }
            }
        )

        GameInfoDivider()

        GameInfoRow(
            title = "Playtime",
            value = if (playtime > 0) {
                "$playtime h Average"
            } else {
                "No data"
            },
            icon = Icons.Default.AccessTime,
            onClick = null
        )
        when (openedDialog) {
            CompanyDialogType.Developers -> {
                CompanySelectionDialog(
                    title = "Developers",
                    companies = developers,
                    onCompanyClick = { developer ->
                        openedDialog = null
                        onDeveloperClick(developer)
                    },
                    onDismissRequest = {
                        openedDialog = null
                    }
                )
            }

            CompanyDialogType.Publishers -> {
                CompanySelectionDialog(
                    title = "Publishers",
                    companies = publishers,
                    onCompanyClick = { publisher ->
                        openedDialog = null
                        onPublisherClick(publisher)
                    },
                    onDismissRequest = {
                        openedDialog = null
                    }
                )
            }

            null -> Unit
        }
    }
}

@Composable
private fun GameInfoRow(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val rowModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (onClick != null || value != "No data") {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (value == "No data") {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onClick != null) {
            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
@Composable
private fun CompanySelectionDialog(
    title: String,
    companies: List<GameCompanyUiModel>,
    onCompanyClick: (GameCompanyUiModel) -> Unit,
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
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                )
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )

            companies.forEachIndexed { index, company ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCompanyClick(company)
                        }
                        .padding(
                            horizontal = 20.dp,
                            vertical = 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = company.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (index != companies.lastIndex) {
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

@Composable
private fun GameInfoDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    )
}
//////////////????????????????????????????????//////////////
//////////////????????PREVIEW SECTION????????////////////
//////////////??????????????????????????????///////////

@Preview(
    name = "Details Dark",
    showBackground = true,
    backgroundColor = 0xFF0B0F1A,
    widthDp = 390,
    heightDp = 850
)
@Composable
private fun DetailsScreenDarkPreview() {
    GameStTheme(
        darkTheme = true
    ) {
        DetailsScreen(
            uiState = GameDetailsUiState.Success(
                game = previewGameDetails
            ),
            onBackClick = {},
            onRetryClick = {},
            modifier = Modifier,
            onDeveloperClick = {},
            onAgeRatingClick = {},
            onPublisherClick = {},
            onSaveConfirm = { _, _, _ -> },
            onDeleteConfirm = {}
        )
    }
}

@Preview(
    name = "Details Light",
    showBackground = true,
    widthDp = 390,
    heightDp = 850
)
@Composable
private fun DetailsScreenLightPreview() {
    GameStTheme(
        darkTheme = false
    ) {
        DetailsScreen(
            uiState = GameDetailsUiState.Success(
                game = previewGameDetails
            ),
            onBackClick = {},
            onRetryClick = {},
            onDeveloperClick = {},
            modifier = Modifier,
            onAgeRatingClick = {},
            onPublisherClick = {},
            onSaveConfirm = { _, _, _ -> },
            onDeleteConfirm = {}
        )
    }
}

private val previewGameDetails = GameDetailsUiModel(
    id = 1,
    title = "Hades II",
    imageUrl = "",
    description = """
        The first-ever sequel from Supergiant Games builds on the best 
        aspects of the original god-like rogue-like dungeon crawler in 
        an all-new, action-packed, endlessly replayable experience.
    """.trimIndent(),
    releaseDate = "2025-09-25",
    rating = 4.7,
    metacritic = 92,
    genres = listOf(
        GameTagUiModel(1, "Action", "action"),
        GameTagUiModel(2, "RPG", "role-playing-games-rpg"),
        GameTagUiModel(3, "Adventure", "adventure"),
        GameTagUiModel(4, "Indie", "indie"),
        GameTagUiModel(5, "Strategy", "strategy"),
        GameTagUiModel(6, "Shooter", "shooter")
    ),
    platforms = listOf(
        "PC",
        "PlayStation 5",
        "Xbox Series S/X",
        "Nintendo Switch"
    ),
    developers = listOf(
        GameCompanyUiModel(
            id = 1,
            name = "Supergiant Games",
            slug = "supergiant-games"
        ),
        GameCompanyUiModel(
            id = 2,
            name = "Monolith Games",
            slug = "monolith-games"
        )
    ),
    publishers = listOf(
        GameCompanyUiModel(
            id = 1,
            name = "Supergiant Games",
            slug = "supergiant-games"
        )
    ),
    screenshots = listOf(
        "https://media.rawg.io/media/screenshots/example1.jpg",
        "https://media.rawg.io/media/screenshots/example2.jpg",
        "https://media.rawg.io/media/screenshots/example3.jpg",
        "https://media.rawg.io/media/screenshots/example4.jpg"
    ),
    isSaved = false,
    ageRating = null,

    playtime = 24,
)
@Preview(
    name = "Details Error",
    showBackground = true,
    widthDp = 390,
    heightDp = 850
)
@Composable
private fun DetailsErrorPreview() {
    GameStTheme(darkTheme = true) {
        DetailsScreen(
            uiState = GameDetailsUiState.Error(
                message = "RAWG is temporarily unavailable. Please try again later."
            ),
            onBackClick = {},
            onRetryClick = {},
            modifier = Modifier,
            onDeveloperClick = {},
            onAgeRatingClick = {},
            onPublisherClick = { },
            onSaveConfirm = { _, _, _ -> },
            onDeleteConfirm = {}
        )
    }
}
// Loading Screen
//GameDetailsUiState.Loading -> {
//    DetailsLoadingScreen(
//        onBackClick = onBackClick,
//        modifier = modifier
//    )
//}
//@Composable
//private fun DetailsLoadingScreen(
//    onBackClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier.fillMaxSize()
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.TopStart),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconButton(
//                onClick = onBackClick,
//                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                    contentDescription = "Back"
//                )
//            }
//
//            Text(
//                text = "GameShelf",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//
//        Column(
//            modifier = Modifier.align(Alignment.Center),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            CircularProgressIndicator()
//
//            Text(
//                text = "Loading game details...",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}
//@Preview(
//    name = "Details Loading",
//    showBackground = true,
//    widthDp = 390,
//    heightDp = 850
//)
//@Composable
//private fun DetailsLoadingPreview() {
//    GameStTheme(darkTheme = true) {
//        DetailsScreen(
//            uiState = GameDetailsUiState.Loading,
//            onBackClick = {},
//            onRetryClick = {}
//        )
//    }
//}