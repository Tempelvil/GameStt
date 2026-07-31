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
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
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

@Composable
fun DetailsScreen(
    uiState: GameDetailsUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(20.dp)
                )
            }
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
                            modifier = Modifier.weight(1f).padding(top=4.dp)
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
                            // позже подключим Room
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
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Save to collection")
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
            onBackClick = {}
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
            onBackClick = {}
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
    ageRating = GameAgeRatingUiModel(
        id = 4,
        name = "Mature",
        slug = "mature"
    ),

    playtime = 24,
)