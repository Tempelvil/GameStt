package com.example.gamest.data.network.steam

import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {

    @GET("steam/player-summaries")
    suspend fun getPlayerSummaries(
        @Query("steamIds") steamIds: String
    ): SteamPlayerSummariesResponseDto

    @GET("steam/owned-games")
    suspend fun getOwnedGames(
        @Query("steamId") steamId: String
    ): SteamOwnedGamesResponseDto

    @GET("steam/recently-played")
    suspend fun getRecentlyPlayedGames(
        @Query("steamId") steamId: String
    ): SteamRecentlyPlayedResponseDto

    @GET("steam/resolve-vanity")
    suspend fun resolveVanityUrl(
        @Query("vanityUrl") vanityUrl: String
    ): SteamVanityResponseDto
}
