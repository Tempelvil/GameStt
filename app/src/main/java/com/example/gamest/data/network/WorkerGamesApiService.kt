package com.example.gamest.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkerGamesApiService {

    @GET("games")
    suspend fun getGames(
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null,
        @Query("genreId") genreId: Int? = null,
        @Query("platformId") platformId: Int? = null,
        @Query("developerId") developerId: Int? = null,
        @Query("publisherId") publisherId: Int? = null,
        @Query("ageRatingCategoryId") ageRatingCategoryId: Int? = null,
        @Query("minimumRatings") minimumRatings: Int? = null,
        @Query("topOnly") topOnly: Boolean? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): IgdbGamesPageDto

    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") gameId: Int
    ): IgdbGameDetailsResponseDto

    @GET("genres")
    suspend fun getGenres(): IgdbGenresResponseDto
}
