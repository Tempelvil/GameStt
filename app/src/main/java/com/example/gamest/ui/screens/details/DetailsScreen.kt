package com.example.gamest.ui.screens.details

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
                }

                item {
                    GameScreenshotPager(
                        screenshots = game.screenshots,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                item {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
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
            contentPadding = PaddingValues(horizontal = 36.dp),
            pageSpacing = 12.dp,
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
                        width = 1.dp,
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
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp
                    )
                )
                .background(MaterialTheme.colorScheme.background)
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
        GameTagUiModel(
            id = 1,
            name = "RPG",
            slug = "role-playing-games-rpg"
        ),
        GameTagUiModel(
            id = 2,
            name = "Action",
            slug = "action"
        ),
        GameTagUiModel(
            id = 3,
            name = "Indie",
            slug = "indie"
        )
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
    isSaved = false
)