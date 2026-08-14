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
import com.example.gamest.ui.screens.statistics.StatisticsScreen
import com.example.gamest.ui.screens.statistics.StatisticsViewModel
import com.example.gamest.ui.screens.steam.SteamConnectionDialog
import com.example.gamest.ui.screens.steam.SteamConnectionViewModel
import com.example.gamest.ui.screens.steam.library.SteamLibraryScreen
import com.example.gamest.ui.screens.steam.library.SteamLibraryViewModel

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

    val isSecondaryScreen =
        currentRoute == GameDestination.DETAILS ||
            currentRoute == GameDestination.STEAM_LIBRARY
    val isSearchScreen = currentRoute== GameDestination.SEARCH

    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory
    )

    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModel.Factory
    )
    val statisticsUiState by statisticsViewModel.uiState
        .collectAsStateWithLifecycle()

    val steamConnectionViewModel: SteamConnectionViewModel = viewModel(
        factory = SteamConnectionViewModel.Factory
    )
    val steamConnectionUiState by steamConnectionViewModel.uiState
        .collectAsStateWithLifecycle()
    var showSteamConnectionDialog by rememberSaveable {
        mutableStateOf(false)
    }

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
            if(!isSecondaryScreen) {
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
                        navController.navigate(
                            GameDestination.createDetailsRoute(it)
                        )
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

                    onSortClick = { sortType->
                        collectionViewModel.selectSort(sortType)
                    },

                    onSteamClick = {
                        if (steamConnectionUiState.isConnected) {
                            navController.navigate(
                                GameDestination.STEAM_LIBRARY
                            )
                        } else {
                            showSteamConnectionDialog = true
                        }
                    },

                    onOpenGame = { gameId ->
                        navController.navigate(
                            GameDestination.createDetailsRoute(gameId)
                        )
                    },

                    onEditConfirm = { gameId, status, userRating, completionStyle, hoursPlayed ->
                        collectionViewModel.updateGame(
                            gameId = gameId,
                            status = status,
                            userRating = userRating,
                            completionStyle = completionStyle,
                            hoursPlayed = hoursPlayed
                        )
                    },

                    onDeleteGame = {gameId ->
                        collectionViewModel.deleteGame(gameId)
                    }
                )

                if (showSteamConnectionDialog) {
                    SteamConnectionDialog(
                        uiState = steamConnectionUiState,
                        onProfileUrlChange ={ profileUrl->
                            steamConnectionViewModel.onProfileUrlChange(profileUrl)
                                            },
                        onCheckConnection ={
                            steamConnectionViewModel.checkConnection()
                                           },
                        onDismissRequest = {
                            showSteamConnectionDialog = false
                            steamConnectionViewModel.reset()
                        },
                        onConnect = {
                            steamConnectionViewModel.connectProfile {
                                showSteamConnectionDialog = false
                                navController.navigate(
                                    GameDestination.STEAM_LIBRARY
                                )
                            }
                        },
                        onDisconnect = {
                            steamConnectionViewModel.disconnectProfile()
                        }
                    )
                }
            }

            composable(GameDestination.STATISTICS) {
                StatisticsScreen(
                    uiState = statisticsUiState,
                    onSectionClick = statisticsViewModel::selectSection,
                    onSteamPeriodClick =
                        statisticsViewModel::selectSteamPeriod,
                    onSteamProfileClick =
                        statisticsViewModel::selectSteamProfile,
                    onGameClick = { gameId ->
                        navController.navigate(
                            GameDestination.createDetailsRoute(gameId)
                        )
                    }
                )
            }

            composable(GameDestination.STEAM_LIBRARY) {
                val steamLibraryViewModel: SteamLibraryViewModel = viewModel(
                    factory = SteamLibraryViewModel.Factory
                )
                val steamLibraryUiState by steamLibraryViewModel.uiState
                    .collectAsStateWithLifecycle()

                SteamLibraryScreen(
                    uiState = steamLibraryUiState,
                    onBackClick = { navController.popBackStack() },
                    onSearchQueryChange =
                        steamLibraryViewModel::onSearchQueryChange,
                    onFilterClick = steamLibraryViewModel::selectFilter,
                    onSortClick = steamLibraryViewModel::selectSort,
                    onSyncClick = steamLibraryViewModel::sync,
                    onAddProfileClick = {
                        steamConnectionViewModel.beginAddingProfile()
                        showSteamConnectionDialog = true
                        navController.popBackStack()
                    },
                    onActivateProfile =
                        steamLibraryViewModel::activateProfile,
                    onPauseProfile =
                        steamLibraryViewModel::pauseActiveProfile,
                    onUnlinkProfile = { onComplete ->
                        steamLibraryViewModel.unlinkActiveProfile(onComplete)
                    },
                    onDeleteProfile = { profile, onComplete ->
                        steamLibraryViewModel.deleteProfileData(
                            profile,
                            onComplete
                        )
                    },
                    onProfileDetached = {
                        navController.popBackStack()
                    }
                )
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
                        onGenreClick = { genre ->
                            searchViewModel.applyFilter(
                                GameFilter.Genres(
                                    id = genre.id,
                                    name = genre.name,
                                    slug = genre.slug
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
                        onSaveConfirm = { status, userRating, completionStyle, hoursPlayed ->
                            detailsViewModel.saveGame(
                                status = status,
                                userRating = userRating,
                                completionStyle = completionStyle,
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
