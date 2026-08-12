package com.example.gamest.data.network.steam

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SteamPlayerSummariesResponseDto(
    val response: SteamPlayerSummariesPayloadDto =
        SteamPlayerSummariesPayloadDto()
)

@Serializable
data class SteamPlayerSummariesPayloadDto(
    val players: List<SteamPlayerDto> = emptyList()
)

@Serializable
data class SteamPlayerDto(
    val steamid: String,
    val personaname: String = "Steam user",
    val profileurl: String? = null,
    val avatarfull: String? = null
)

@Serializable
data class SteamOwnedGamesResponseDto(
    val response: SteamOwnedGamesPayloadDto = SteamOwnedGamesPayloadDto()
)

@Serializable
data class SteamOwnedGamesPayloadDto(
    @SerialName("game_count")
    val gameCount: Int? = null,
    val games: List<SteamGameDto> = emptyList()
)

@Serializable
data class SteamRecentlyPlayedResponseDto(
    val response: SteamRecentlyPlayedPayloadDto =
        SteamRecentlyPlayedPayloadDto()
)

@Serializable
data class SteamRecentlyPlayedPayloadDto(
    @SerialName("total_count")
    val totalCount: Int? = null,
    val games: List<SteamGameDto> = emptyList()
)

@Serializable
data class SteamGameDto(
    val appid: Int,
    val name: String = "Unknown game",
    @SerialName("playtime_forever")
    val playtimeForever: Long = 0,
    @SerialName("playtime_2weeks")
    val playtimeTwoWeeks: Long? = null,
    @SerialName("img_icon_url")
    val iconHash: String? = null,
    @SerialName("rtime_last_played")
    val lastPlayedAt: Long? = null
)

@Serializable
data class SteamVanityResponseDto(
    val response: SteamVanityPayloadDto = SteamVanityPayloadDto()
)

@Serializable
data class SteamVanityPayloadDto(
    val steamid: String? = null,
    val success: Int = 0,
    val message: String? = null
)
