package com.example.gamest.data.repository

import com.example.gamest.BuildConfig
import com.example.gamest.data.network.RawgApiService
import com.example.gamest.data.network.RawgGameDto
import com.example.gamest.model.GameUiModel

class GamesRepository(
    private val apiService: RawgApiService,
    private val apiKey: String
) {

    suspend fun getTopRatedGames(): List<GameUiModel> {
        println("RAWG KEY = ${BuildConfig.RAWG_API_KEY}")
        val response = apiService.getGames(
            apiKey = apiKey,
            pageSize = 20,
            ordering = "-rating"
        )

        return response.listGame.map { it.toGameUiModel() }
    }
}

private fun RawgGameDto.toGameUiModel(): GameUiModel {
    return GameUiModel(
        id = id,
        title = name,
        imageUrl = backgroundImage ?: "",
        rating = rating,
        genres = genres.map { it.name },
        platforms = platforms.map { it.platform.name },
        isSaved = false
    )
}