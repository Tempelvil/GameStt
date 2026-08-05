package com.example.gamest.ui.screens.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamest.GameStApplication
import com.example.gamest.data.local.GameStatus
import com.example.gamest.data.mapper.toGameDetailsUiModel
import com.example.gamest.data.mapper.toGameEntity
import com.example.gamest.data.repository.GamesRepository
import com.example.gamest.data.repository.LocalGamesRepository
import com.example.gamest.model.GameDetailsUiModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class GameDetailsViewModel(
    private val gameId: Int,
    private val gamesRepository: GamesRepository,
    private val localGamesRepository: LocalGamesRepository
) : ViewModel() {

    var uiState by mutableStateOf<GameDetailsUiState>(
        GameDetailsUiState.Loading
    )
        private set

    private var currentGame: GameDetailsUiModel? = null

    init {
        observeGame()
    }

    private fun observeGame() {
        viewModelScope.launch {
            localGamesRepository
                .observeGameById(gameId)
                .collectLatest { localGame ->

                    if (localGame != null) {
                        val game = localGame.toGameDetailsUiModel()

                        currentGame = game
                        uiState = GameDetailsUiState.Success(
                            game = game
                        )
                    } else {
                        val alreadyLoadedGame = currentGame

                        if (alreadyLoadedGame != null) {
                            val unsavedGame = alreadyLoadedGame.copy(
                                isSaved = false
                            )

                            currentGame = unsavedGame
                            uiState = GameDetailsUiState.Success(
                                game = unsavedGame
                            )
                        } else {
                            loadRemoteGameDetails()
                        }
                    }
                }
        }
    }

    private suspend fun loadRemoteGameDetails() {
        uiState = GameDetailsUiState.Loading

        uiState = try {
            val game = gamesRepository
                .getGameDetails(gameId)
                .copy(isSaved = false)

            currentGame = game

            GameDetailsUiState.Success(
                game = game
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            GameDetailsUiState.Error(
                message = getHttpErrorMessage(e.code())
            )
        } catch (e: IOException) {
            GameDetailsUiState.Error(
                message = "Unable to connect to RAWG. Check your internet connection and try again."
            )
        } catch (e: Exception) {
            GameDetailsUiState.Error(
                message = "An unexpected error occurred. Please try again."
            )
        }
    }

    fun saveGame(
        status: GameStatus,
        userRating: Int?,
        hoursPlayed: Int
    ) {
        val game = currentGame ?: return

        viewModelScope.launch {
            localGamesRepository.saveGame(
                game.toGameEntity(
                    status = status,
                    userRating = userRating,
                    hoursPlayed = hoursPlayed
                )
            )
        }
    }
    fun deleteGame() {
        viewModelScope.launch {
            val savedGame =
                localGamesRepository.getGameById(gameId)
                    ?: return@launch

            localGamesRepository.deleteGame(savedGame)
        }
    }
    fun retry() {
        viewModelScope.launch {
            loadRemoteGameDetails()
        }
    }

    private fun getHttpErrorMessage(code: Int): String {
        return when (code) {
            401 -> "The RAWG API key is invalid."
            403 -> "Access to the RAWG API is denied."
            404 -> "Game information was not found."
            429 -> "The RAWG request limit has been exceeded."

            500, 502, 503, 504, 522 ->
                "RAWG is temporarily unavailable. Please try again later."

            else -> "Server error. HTTP code: $code"
        }
    }

    companion object {

        fun factory(
            gameId: Int
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    val application =
                        this[APPLICATION_KEY] as GameStApplication

                    GameDetailsViewModel(
                        gameId = gameId,
                        gamesRepository =
                            application.container.gamesRepository,
                        localGamesRepository =
                            application.container.localGamesRepository
                    )
                }
            }
        }
    }
}