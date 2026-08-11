package com.example.gamest.data.repository

import com.example.gamest.data.network.steam.SteamApiService
import com.example.gamest.data.network.steam.SteamGameDto

interface SteamRepository {
    suspend fun checkConnection(
        profileUrl: String
    ): SteamConnectionResult
}

data class SteamConnectionResult(
    val steamId: String,
    val ownedGamesCount: Int,
    val totalPlaytimeMinutes: Long,
    val recentlyPlayedCount: Int,
    val games: List<SteamGame>
)

data class SteamGame(
    val appId: Int,
    val name: String,
    val totalPlaytimeMinutes: Long,
    val recentPlaytimeMinutes: Long?,
    val lastPlayedAt: Long?
)

class DefaultSteamRepository(
    private val apiService: SteamApiService,
    private val apiKey: String
) : SteamRepository {

    override suspend fun checkConnection(
        profileUrl: String
    ): SteamConnectionResult {
        if (apiKey.isBlank()) {
            throw SteamConfigurationException(
                "Steam API key is missing from local.properties."
            )
        }

        val reference = SteamProfileUrlParser.parse(profileUrl)
        val steamId = resolveSteamId(reference)

        val ownedResponse = apiService.getOwnedGames(
            apiKey = apiKey,
            steamId = steamId
        ).response

        val ownedGamesCount = ownedResponse.gameCount
            ?: throw SteamGamesUnavailableException(
                "Steam found the profile, but its Game details are not available. Make them public in Steam privacy settings."
            )

        val recentlyPlayedResponse = apiService.getRecentlyPlayedGames(
            apiKey = apiKey,
            steamId = steamId
        ).response

        val ownedGames = ownedResponse.games
            .map(SteamGameDto::toSteamGame)
        val recentlyPlayedGames = recentlyPlayedResponse.games
            .map(SteamGameDto::toSteamGame)
        val gamesForDisplay = recentlyPlayedGames
            .ifEmpty {
                ownedGames.sortedByDescending { game ->
                    game.totalPlaytimeMinutes
                }
            }
            .take(MAX_DISPLAY_GAMES)

        return SteamConnectionResult(
            steamId = steamId,
            ownedGamesCount = ownedGamesCount,
            totalPlaytimeMinutes = ownedGames.sumOf { game ->
                game.totalPlaytimeMinutes
            },
            recentlyPlayedCount = recentlyPlayedResponse.totalCount
                ?: recentlyPlayedGames.size,
            games = gamesForDisplay
        )
    }

    private suspend fun resolveSteamId(
        reference: SteamProfileReference
    ): String {
        return when (reference.type) {
            SteamProfileReferenceType.STEAM_ID -> reference.value

            SteamProfileReferenceType.VANITY_NAME -> {
                val response = apiService.resolveVanityUrl(
                    apiKey = apiKey,
                    vanityUrl = reference.value
                ).response

                response.steamid
                    ?.takeIf { steamId -> response.success == 1 }
                    ?: throw SteamProfileNotFoundException(
                        response.message
                            ?: "Steam could not find this profile."
                    )
            }
        }
    }

    private companion object {
        const val MAX_DISPLAY_GAMES = 10
    }
}

private fun SteamGameDto.toSteamGame(): SteamGame {
    return SteamGame(
        appId = appid,
        name = name,
        totalPlaytimeMinutes = playtimeForever.coerceAtLeast(0),
        recentPlaytimeMinutes = playtimeTwoWeeks
            ?.coerceAtLeast(0),
        lastPlayedAt = lastPlayedAt
    )
}

class SteamConfigurationException(
    message: String
) : IllegalStateException(message)

class SteamProfileNotFoundException(
    message: String
) : IllegalArgumentException(message)

class SteamGamesUnavailableException(
    message: String
) : IllegalStateException(message)
