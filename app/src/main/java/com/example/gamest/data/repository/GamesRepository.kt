package com.example.gamest.data.repository

import com.example.gamest.BuildConfig
import com.example.gamest.data.network.RawgApiService
import com.example.gamest.data.network.RawgGameDto
import com.example.gamest.model.GameFilter
import com.example.gamest.model.GameUiModel

class GamesRepository(
    private val apiService: RawgApiService,
    private val apiKey: String
){

    suspend fun getGames(
        searchQuery: String,
        filter: GameFilter,
        page: Int
    ):List<GameUiModel>{
        val search = searchQuery.takeIf{it.isNotBlank()}

        val genres = when{
            search !=null ->null
            filter == GameFilter.Rpg -> "role-playing-games-rpg"
            filter == GameFilter.Action -> "action"
            else -> null
        }
        val platforms = when{
            search !=null ->null
            filter == GameFilter.Pc -> "4"
            else -> null
        }
        val ordering = when {
            search != null -> null
            filter == GameFilter.TopRated -> "-metacritic"
            else -> "-metacritic"
        }
        val response = apiService.getGames(
            apiKey = apiKey,
            pageSize = 20,
            search = search,
            Page = page,
            genres = genres,
            platforms = platforms,
            ordering = ordering
        )

        return response.listGame.map {it.toGameUiModel()}
    }
}

private fun RawgGameDto.toGameUiModel(): GameUiModel {
    return GameUiModel(
        id = id,
        title = name,
        imageUrl = backgroundImage ?: "",
        rating = rating,
        genres = genres?.map { it.name }?: emptyList(),
        platforms = platforms?.map { it.platform.name }?: emptyList(),
        isSaved = false,
        metacritic = metacritic,

    )
}