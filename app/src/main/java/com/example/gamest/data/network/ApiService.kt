package com.example.gamest.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApiService {
    @GET("games")
    suspend fun getGames(
        @Query("key") apiKey: String,
        @Query("page") Page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("ordering") ordering: String? = "-rating"
    ): RawgGamesResponseDto

    @GET("games/{id}")
    suspend fun getGamesDetails(
        @Path("id")gameId: Int,
        @Query("key") apiKey:String
    ): RawgGameDetailsDto
}