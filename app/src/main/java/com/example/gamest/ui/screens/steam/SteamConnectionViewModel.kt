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
import com.example.gamest.data.repository.SteamProfileLimitException
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

    private var restoredSteamId: String? = null
    private var pendingConnectionResult: SteamConnectionResult? = null
    private var previousProfileUrl: String = ""

    private val _uiState = MutableStateFlow(
        SteamConnectionUiState()
    )
    val uiState: StateFlow<SteamConnectionUiState> =
        _uiState.asStateFlow()

    fun onProfileUrlChange(profileUrl: String) {
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
        if (_uiState.value.isLoading || profileUrl.isBlank()) {
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
                pendingConnectionResult = result

                if (
                    _uiState.value.isConnected &&
                    !_uiState.value.isAddingProfile
                ) {
                    steamRepository.saveProfileAndLibrary(result)
                    saveConnection(
                        profileUrl = result.canonicalProfileUrl,
                        steamId = result.steamId,
                        personaName = result.personaName,
                        avatarUrl = result.avatarUrl
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        profileUrl = result.canonicalProfileUrl,
                        isLoading = false,
                        result = result.toUiModel(),
                        errorMessage = null
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
            } catch (exception: SteamProfileLimitException) {
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
    fun connectProfile(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        val result = state.result ?: return
        val repositoryResult = pendingConnectionResult ?: return

        viewModelScope.launch {
            try {
                steamRepository.saveProfileAndLibrary(repositoryResult)
                saveConnection(
                    profileUrl = result.canonicalProfileUrl,
                    steamId = result.steamId,
                    personaName = result.personaName,
                    avatarUrl = result.avatarUrl
                )
                _uiState.update { current ->
                    current.copy(isAddingProfile = false)
                }
                onSuccess()
            } catch (exception: SteamProfileLimitException) {
                showError(exception.message)
            }
        }
    }

    fun beginAddingProfile() {
        previousProfileUrl = _uiState.value.profileUrl
        pendingConnectionResult = null
        _uiState.update { state ->
            state.copy(
                profileUrl = "",
                result = null,
                errorMessage = null,
                isAddingProfile = true
            )
        }
    }

    fun disconnectProfile() {
        viewModelScope.launch {
            _uiState.value.connectedSteamId?.let { steamId ->
                steamRepository.updateProfileStatus(
                    steamId,
                    com.example.gamest.data.local.SteamProfileStatus.UNLINKED
                )
            }
            steamConnectionPreferences.clearConnection()
            restoredSteamId = null
            pendingConnectionResult = null

            _uiState.value = SteamConnectionUiState()
        }
    }

    fun reset() {
        _uiState.update { state ->
            state.copy(
                profileUrl = if (state.isAddingProfile) {
                    previousProfileUrl
                } else {
                    state.profileUrl
                },
                errorMessage = null,
                isLoading = false,
                result = if (state.isAddingProfile) null else state.result,
                isAddingProfile = false
            )
        }
    }

    private fun showError(message: String?) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                result = if (state.isConnected) state.result else null,
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
                        connectedPersonaName = connection.personaName
                            .takeIf { it.isNotBlank() },
                        connectedAvatarUrl = connection.avatarUrl
                            .takeIf { it.isNotBlank() },
                        lastSyncAt = connection.lastSyncAt,
                        profileUrl = if (connection.isConnected) {
                            connection.profileUrl
                        } else {
                            state.profileUrl
                        }
                    )
                }

                if (
                    connection.isConnected &&
                    connection.profileUrl.isNotBlank() &&
                    restoredSteamId != connection.steamId
                ) {
                    restoredSteamId = connection.steamId
                    checkConnection()
                }
            }
        }
    }

    private suspend fun saveConnection(
        profileUrl: String,
        steamId: String,
        personaName: String,
        avatarUrl: String?
    ) {
        restoredSteamId = steamId
        steamConnectionPreferences.saveConnection(
            profileUrl = profileUrl,
            steamId = steamId,
            personaName = personaName,
            avatarUrl = avatarUrl
        )
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
        personaName = personaName,
        avatarUrl = avatarUrl,
        canonicalProfileUrl = canonicalProfileUrl,
        ownedGamesCount = ownedGamesCount,
        totalPlaytimeMinutes = totalPlaytimeMinutes,
        recentlyPlayedCount = recentlyPlayedCount,
        games = games.map(SteamGame::toUiModel)
            .take(MAX_DIALOG_GAMES)
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

private const val MAX_DIALOG_GAMES = 10
