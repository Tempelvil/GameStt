package com.example.gamest.ui.screens.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gamest.model.GameUiModel
import com.example.gamest.ui.theme.GameStTheme

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onSearchQueryChange: (String) -> Unit,
    onSaveGameClick: (Int) -> Unit,
    onGenreClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
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
            Column {
                Text(
                    text = if (uiState.searchQuery.isBlank()) {
                        "Top Rated"
                    } else {
                        "Search Results"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (uiState.searchQuery.isBlank()) {
                        "The highest rated games by the community"
                    } else {
                        "Games matching your search"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            GenreChipsRow(
                selectedGenre = uiState.selectedGenre,
                onGenreClick = onGenreClick
            )
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
    selectedGenre: String?,
    onGenreClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val genres = listOf("Top Rated", "RPG", "Action", "PC", "Adventure")

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.forEach { genre ->
            FilterChip(
                selected = selectedGenre == genre,
                onClick = { onGenreClick(genre) },
                label = { Text(genre) }
            )
        }
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
                )
            ),
            onSearchQueryChange = {},
            onSaveGameClick = {},
            onGenreClick = {},
            modifier = Modifier,
        )
    }
}