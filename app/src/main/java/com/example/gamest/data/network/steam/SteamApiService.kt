package com.example.gamest.data.network.steam

import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {

    @GET("IPlayerService/GetOwnedGames/v0001/")
    suspend fun getOwnedGames(
        @Query("key") apiKey: String,
        @Query("steamid") steamId: String,
        @Query("include_appinfo") includeAppInfo: Boolean = true,
        @Query("include_played_free_games") includePlayedFreeGames: Boolean = true,
        @Query("format") format: String = "json"
    ): SteamOwnedGamesResponseDto

    @GET("IPlayerService/GetRecentlyPlayedGames/v0001/")
    suspend fun getRecentlyPlayedGames(
        @Query("key") apiKey: String,
        @Query("steamid") steamId: String,
        @Query("count") count: Int = 0,
        @Query("format") format: String = "json"
    ): SteamRecentlyPlayedResponseDto

    @GET("ISteamUser/ResolveVanityURL/v0001/")
    suspend fun resolveVanityUrl(
        @Query("key") apiKey: String,
        @Query("vanityurl") vanityUrl: String,
        @Query("url_type") urlType: Int = 1,
        @Query("format") format: String = "json"
    ): SteamVanityResponseDto
}
