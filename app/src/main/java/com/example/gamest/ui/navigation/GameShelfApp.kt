package com.example.gamest.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamest.ui.components.GameBottomBar
import com.example.gamest.ui.components.GameTopBar
import com.example.gamest.ui.screens.search.SearchScreen
import com.example.gamest.ui.screens.search.SearchViewModel

@Composable
fun GameShelfApp(
    isDarkTheme: Boolean,
    onThemeClick: () -> Unit
) {
    var selectedRoute by rememberSaveable { mutableStateOf("search") }

    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory
    )

    val title = when (selectedRoute) {
        "search" -> "GameShelf"
        "collection" -> "My Collection"
        "statistics" -> "Statistics"
        else -> "GameShelf"
    }

    Scaffold(
        topBar = {
            GameTopBar(
                title = title,
                isDarkTheme = isDarkTheme,
                onThemeClick = onThemeClick,
                showLogo = selectedRoute == "search"
            )
        },
        bottomBar = {
            GameBottomBar(
                selectedRoute = selectedRoute,
                onItemClick = { selectedRoute = it }
            )
        }
    ) { innerPadding ->
        when (selectedRoute) {
            "search" -> SearchScreen(
                uiState = searchViewModel.uiState,
                onSearchQueryChange = { query ->
                    searchViewModel.onSearchQueryChange(query)
                },
                onSaveGameClick = { gameId ->
                    searchViewModel.onSaveGameClick(gameId)
                },
                modifier = Modifier.padding(innerPadding),
                onGenreClick = { genre ->
                    searchViewModel.onGenreClick(genre)
                },
                onMoreGenreClick = {}
            )

            "collection" -> Text("Collection screen")
            "statistics" -> Text("Statistics screen")
        }
    }
}