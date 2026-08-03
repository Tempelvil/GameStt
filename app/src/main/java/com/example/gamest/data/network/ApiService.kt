package com.example.gamest.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApiService {
    @GET("games")
    suspend fun getGames(
        @Query("key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Query("genres") genres: String? = null,
        @Query("developers") developers: String? = null,
        @Query("publishers") publishers: String? = null,
        @Query("esrb_rating") esrbRating: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("ordering") ordering: String? = "-rating"
    ): RawgGamesResponseDto

    @GET("games/{id}")
    suspend fun getGamesDetails(
        @Path("id")gameId: Int,
        @Query("key") apiKey:String
    ): RawgGameDetailsDto

    @GET("games/{id}/screenshots")
    suspend fun getScreenshots(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String,
        @Query("page_size") pageSize: Int = 4

    ): RawgScreenshotsDto

    @GET("genres")
    suspend fun getGenres(
        @Query("key") apiKey: String,
        @Query("page_size") pageSize: Int = 50
    ): RawgGenresResponseDto
}