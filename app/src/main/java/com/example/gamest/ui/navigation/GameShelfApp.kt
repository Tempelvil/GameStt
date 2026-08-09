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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gamest.model.GameFilter
import com.example.gamest.ui.components.GameBottomBar
import com.example.gamest.ui.components.GameTopBar
import com.example.gamest.ui.screens.collection.CollectionScreen
import com.example.gamest.ui.screens.collection.CollectionViewModel
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

    val collectionViewModel: CollectionViewModel = viewModel(
        factory = CollectionViewModel.Factory
    )
    val collectionUiState by collectionViewModel.uiState
        .collectAsStateWithLifecycle()

    val isDetailScreen = currentRoute == GameDestination.DETAILS
    val isSearchScreen = currentRoute== GameDestination.SEARCH

    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory
    )

    Scaffold(
        topBar = {
            if(isSearchScreen){
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

                    onSearchQueryChange = {
                        searchViewModel.onSearchQueryChange(it)
                    },

                    onSaveGameClick = {
                        searchViewModel.onSaveGameClick(it)
                    },

                    onGenreClick = { filter ->
                        searchViewModel.applyFilter(filter)
                    },

                    onMoreGenreClick = {
                        searchViewModel.loadGenres()
                    },

                    onGenreSelected = { genre ->
                        searchViewModel.applyFilter(
                            GameFilter.Genres(
                                id = genre.id,
                                name = genre.name,
                                slug = genre.slug
                            )
                        )
                    },

                    onRetryGenresClick = {
                        searchViewModel.loadGenres()
                    },

                    onLoadNextPage = {
                        searchViewModel.loadNextPage()
                    },

                    onGameClick = { gameId ->
                        navController.navigate(
                            GameDestination.createDetailsRoute(gameId)
                        )
                    }
                )
            }
            composable(GameDestination.COLLECTION) {
                CollectionScreen(
                    uiState = collectionUiState,

                    onFilterClick = { filter ->
                        collectionViewModel.selectFilter(filter)
                    },

                    onSortClick = {
                        // SortDialog следующим шагом
                    },

                    onSteamClick = {
                        // Steam dialog позже
                    },

                    onOpenGame = { gameId ->
                        navController.navigate(
                            GameDestination.createDetailsRoute(gameId)
                        )
                    },

                    onEditConfirm = { gameId, status, userRating, hoursPlayed ->
                        collectionViewModel.updateGame(
                            gameId = gameId,
                            status = status,
                            userRating = userRating,
                            hoursPlayed = hoursPlayed
                        )
                    },

                    onDeleteGame = { gameId ->
                        // следующим шагом
                    }
                )
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
                        },
                        onRetryClick = { detailsViewModel.retry() },
                        modifier = Modifier,
                        onDeveloperClick = { developer ->
                            searchViewModel.applyFilter(
                                GameFilter.Developer(
                                    id = developer.id,
                                    name = developer.name
                                )
                            )

                            navController.navigate(GameDestination.SEARCH) {
                                popUpTo(GameDestination.SEARCH) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        onAgeRatingClick = {ageRating ->
                            searchViewModel.applyFilter(
                                GameFilter.AgeRating(
                                    id = ageRating.id,
                                    name = ageRating.name
                                )
                            )
                            navController.navigate(GameDestination.SEARCH){
                                popUpTo(GameDestination.SEARCH){
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        onSaveConfirm = { status, userRating, hoursPlayed ->
                            detailsViewModel.saveGame(
                                status = status,
                                userRating = userRating,
                                hoursPlayed = hoursPlayed
                            )
                        },
                        onDeleteConfirm = {
                            detailsViewModel.deleteGame()
                        },
                        onPublisherClick = {publisher ->
                            searchViewModel.applyFilter(
                                GameFilter.Publisher(
                                    id = publisher.id,
                                    name = publisher.name
                                )
                            )

                            navController.navigate(GameDestination.SEARCH){
                                popUpTo(GameDestination.SEARCH){
                                    inclusive = false
                                }
                                launchSingleTop = false
                            }
                        },
                    )
                }else{
                    Text("Game id not found")
                }


            }

        }
    }
}
