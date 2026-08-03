package com.example.gamest.data.repository

import com.example.gamest.data.network.RawgApiService
import com.example.gamest.data.network.RawgGameDetailsDto
import com.example.gamest.data.network.RawgGameDto
import com.example.gamest.data.network.RawgGamesResponseDto
import com.example.gamest.model.GameAgeRatingUiModel
import com.example.gamest.model.GameCompanyUiModel
import com.example.gamest.model.GameDetailsUiModel
import com.example.gamest.model.GameFilter
import com.example.gamest.model.GameTagUiModel
import com.example.gamest.model.GameUiModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class GamesRepository(
    private val apiService: RawgApiService,
    private val apiKey: String
){

    suspend fun getGameDetails(
        gameId: Int
    ): GameDetailsUiModel {

        val detailsResponse = apiService.getGamesDetails(
            gameId = gameId,
            apiKey = apiKey
        )

        val screenshotsResponse = apiService.getScreenshots(
            gameId = gameId,
            apiKey = apiKey,
            pageSize = 4
        )

        val screenshots = screenshotsResponse.screenshots
            .map { it.image
            }
            .ifEmpty {
                listOfNotNull(detailsResponse.backgroundImage)
            }

        return detailsResponse.toGameDetailsUiModel(
            screenshots = screenshots
        )
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
        val developers = when{
            search != null -> null
            filter is GameFilter.Developer -> filter.id.toString()
            else -> null
        }
        val publishers = when{
            search != null ->null
            filter is GameFilter.Publisher -> filter.id.toString()
            else -> null
        }
        val esrbRating = when{
            search != null -> null
            filter is GameFilter.AgeRating -> filter.id.toString()
            else ->null
        }
        val response = apiService.getGames(
            apiKey = apiKey,
            pageSize = 20,
            search = search,
            page = page,
            genres = genres,
            platforms = platforms,
            ordering = ordering,
            esrbRating = esrbRating,
            publishers = publishers,
            developers = developers,
        )


        return response.listGame.map {it.toGameUiModel()}
    }
}

private fun RawgGameDetailsDto.toGameDetailsUiModel(
    screenshots:List<String>
): GameDetailsUiModel {
    return GameDetailsUiModel(
        id = id,
        title = name,
        imageUrl = backgroundImage ?: "",
        description = descriptionRaw,
        releaseDate = formatReleaseDate(released),
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
        },
        playtime = playtime,
        screenshots = screenshots,
        isSaved = false,
        ageRating = esrbRating?.let { rating ->
            GameAgeRatingUiModel(
                id = rating.id,
                name = rating.name,
                slug = rating.slug
            )
        },
    )
}
private fun formatReleaseDate(date: String?): String {
    if (date.isNullOrBlank()) {
        return "Unknown date"
    }

    return try {
        LocalDate
            .parse(date)
            .format(
                DateTimeFormatter.ofPattern(
                    "MMM d, yyyy",
                    Locale.ENGLISH
                )
            )
    } catch (e: DateTimeParseException) {
        date
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