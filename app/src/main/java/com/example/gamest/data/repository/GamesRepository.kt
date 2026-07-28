package com.example.gamest.data.repository

import com.example.gamest.BuildConfig
import com.example.gamest.data.network.RawgApiService
import com.example.gamest.data.network.RawgGameDetailsDto
import com.example.gamest.data.network.RawgGameDto
import com.example.gamest.data.network.RawgGamesResponseDto
import com.example.gamest.model.GameCompanyUiModel
import com.example.gamest.model.GameDetailsUiModel
import com.example.gamest.model.GameFilter
import com.example.gamest.model.GameTagUiModel
import com.example.gamest.model.GameUiModel

class GamesRepository(
    private val apiService: RawgApiService,
    private val apiKey: String
){

    suspend fun getGameDetails(
        gameId: Int
    ): GameDetailsUiModel{
        val response = apiService.getGamesDetails(
            gameId = gameId,
            apiKey = apiKey
        )
        return response.toGameDetailsUiModel()
    }
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
private fun RawgGameDetailsDto.toGameDetailsUiModel(): GameDetailsUiModel {
    return GameDetailsUiModel(
        id = id,
        title = name,
        imageUrl = backgroundImage ?: "",
        description = descriptionRaw,
        releaseDate = released,
        rating = rating,
        metacritic = metacritic,

        genres = genres.map { genre ->
            GameTagUiModel(
                id = genre.id,
                name = genre.name,
                slug = genre.slug
            )
        },

        platforms = platforms
            ?.map { it.platform.name }
            ?: emptyList(),

        developers = developers.map { developer ->
            GameCompanyUiModel(
                id = developer.id,
                name = developer.name,
                slug = developer.slug
            )
        },

        publishers = publishers.map { publisher ->
            GameCompanyUiModel(
                id = publisher.id,
                name = publisher.name,
                slug = publisher.slug
            )
        }
    )
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