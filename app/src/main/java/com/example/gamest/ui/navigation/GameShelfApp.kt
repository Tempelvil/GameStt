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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gamest.ui.components.GameBottomBar
import com.example.gamest.ui.components.GameTopBar
import com.example.gamest.ui.screens.details.DetailsScreen
import com.example.gamest.ui.screens.details.GameDetailsViewModel
import com.example.gamest.ui.screens.search.SearchScreen
import com.example.gamest.ui.screens.search.SearchViewModel

@Composable
fun GameShelfApp(
    isDarkTheme: Boolean,
    onThemeClick: () -> Unit
) {
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = currentBackStackEntry?.destination?.route

    val isDetailScreen = currentRoute == GameDestination.DETAILS

    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory
    )

    Scaffold(
        topBar = {
            if(!isDetailScreen){
                GameTopBar(
                    title = when(currentRoute){
                        GameDestination.COLLECTION ->"My Collection"
                        GameDestination.STATISTICS ->"Statistics"
                        else->"GameShelf"
                    },
                    isDarkTheme = isDarkTheme,
                    onThemeClick = onThemeClick,
                    showLogo = currentRoute == GameDestination.SEARCH
                )
            }
        },
        bottomBar = {
            if(!isDetailScreen) {
                GameBottomBar(
                    selectedRoute = currentRoute ?: GameDestination.SEARCH,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController=navController,
            startDestination = GameDestination.SEARCH,
            modifier = Modifier.padding(innerPadding)
        ){

            composable(GameDestination.SEARCH){
                SearchScreen(
                    uiState = searchViewModel.uiState,
                    onSearchQueryChange = {str->
                        searchViewModel.onSearchQueryChange(str)
                    },
                    onSaveGameClick = {game->searchViewModel.onSaveGameClick(game)},
                    onGenreClick = {filter->searchViewModel.onGenreClick(filter)},
                    onMoreGenreClick = {},
                    onLoadNextPage = {searchViewModel.loadNextPage()},
                    modifier = Modifier,
                    onGameClick= { gameId->
                        navController.navigate(
                            GameDestination.createDetailsRoute(gameId)
                        )
                    },
                )
            }
            composable(GameDestination.COLLECTION) {
                Text("Collection screen")
            }

            composable(GameDestination.STATISTICS) {
                Text("Statistics screen")
            }

            composable(GameDestination.DETAILS) {
                backStackEntry->
                val gameId = backStackEntry.arguments
                    ?.getString(GameDestination.DETAILS_ARGUMENT)
                    ?.toIntOrNull()
                if(gameId!=null) {
                    val detailsViewModel: GameDetailsViewModel = viewModel(
                        factory = GameDetailsViewModel.factory(gameId)
                    )
                    DetailsScreen(
                        uiState = detailsViewModel.uiState,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }else{
                    Text("Game id not found")
                }


            }

        }
    }
}