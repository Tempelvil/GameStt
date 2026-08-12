package com.example.gamest.ui.screens.steam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamest.GameStApplication
import com.example.gamest.data.local.preferences.SteamConnectionPreferences
import com.example.gamest.data.repository.InvalidSteamProfileUrlException
import com.example.gamest.data.repository.SteamConfigurationException
import com.example.gamest.data.repository.SteamConnectionResult
import com.example.gamest.data.repository.SteamGame
import com.example.gamest.data.repository.SteamGamesUnavailableException
import com.example.gamest.data.repository.SteamProfileNotFoundException
import com.example.gamest.data.repository.SteamRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SteamConnectionViewModel(
    private val steamRepository: SteamRepository,
    private val steamConnectionPreferences: SteamConnectionPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SteamConnectionUiState()
    )
    val uiState: StateFlow<SteamConnectionUiState> =
        _uiState.asStateFlow()

    fun onProfileUrlChange(profileUrl: String) {
        println("STEAM INPUT = [$profileUrl]")
        _uiState.update { state ->
            state.copy(
                profileUrl = profileUrl,
                result = null,
                errorMessage = null
            )
        }
    }

    fun checkConnection() {
        val profileUrl = _uiState.value.profileUrl.trim()

        println("STEAM CHECK = [$profileUrl]")

        if (_uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    profileUrl = profileUrl,
                    isLoading = true,
                    result = null,
                    errorMessage = null
                )
            }

            try {
                val result = steamRepository.checkConnection(profileUrl)

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        result = result.toUiModel()
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: InvalidSteamProfileUrlException) {
                showError(exception.message)
            } catch (exception: SteamProfileNotFoundException) {
                showError(exception.message)
            } catch (exception: SteamGamesUnavailableException) {
                showError(exception.message)
            } catch (exception: SteamConfigurationException) {
                showError(exception.message)
            } catch (exception: HttpException) {
                showError(getHttpErrorMessage(exception.code()))
            } catch (_: IOException) {
                showError(
                    "Unable to connect to Steam. Check your internet connection and try again."
                )
            } catch (_: Exception) {
                showError(
                    "Steam returned an unexpected response. Please try again."
                )
            }
        }
    }
    fun connectProfile(){
        val state = _uiState.value
        val result = state.result ?: return

        viewModelScope.launch {
            steamConnectionPreferences.saveConnection(
                profileUrl = state.profileUrl,
                steamId = result.steamId
            )
        }
    }
    fun disconnectProfile(){
        viewModelScope.launch {
            steamConnectionPreferences.clearConnection()

            _uiState.update { state ->
                state.copy(
                    profileUrl = "",
                    result = null,
                    errorMessage = null
                )
            }
        }
    }

    fun reset() {
        _uiState.update { state ->
            state.copy(
                result = null,
                errorMessage = null,
                isLoading = false
            )
        }
    }

    private fun showError(message: String?) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                result = null,
                errorMessage = message
                    ?: "Unable to check this Steam profile."
            )
        }
    }

    private fun getHttpErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Steam rejected the profile request. Check the profile link."
            401, 403 -> "Steam rejected the API key."
            404 -> "Steam could not find this profile."
            429 -> "The Steam request limit has been reached. Try again later."
            500, 502, 503, 504 ->
                "Steam is temporarily unavailable. Try again later."
            else -> "Steam request failed with HTTP code $code."
        }
    }
    init {
        viewModelScope.launch {
            steamConnectionPreferences.connectionData.collect { connection ->

                _uiState.update { state ->
                    state.copy(
                        isConnected = connection.isConnected,
                        connectedSteamId = connection.steamId
                            .takeIf { it.isNotBlank() },
                        lastSyncAt = connection.lastSyncAt,
                        profileUrl = if (connection.isConnected) {
                            connection.profileUrl
                        } else {
                            state.profileUrl
                        }
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[APPLICATION_KEY] as GameStApplication

                SteamConnectionViewModel(
                    steamRepository =
                        application.container.steamRepository,
                    steamConnectionPreferences =
                        application.container.steamConnectionPreferences
                )
            }
        }
    }
}

private fun SteamConnectionResult.toUiModel(): SteamConnectionResultUiModel {
    return SteamConnectionResultUiModel(
        steamId = steamId,
        ownedGamesCount = ownedGamesCount,
        totalPlaytimeMinutes = totalPlaytimeMinutes,
        recentlyPlayedCount = recentlyPlayedCount,
        games = games.map(SteamGame::toUiModel)
    )
}

private fun SteamGame.toUiModel(): SteamGameUiModel {
    return SteamGameUiModel(
        appId = appId,
        name = name,
        totalPlaytimeMinutes = totalPlaytimeMinutes,
        recentPlaytimeMinutes = recentPlaytimeMinutes
    )
}
