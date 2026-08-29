package com.example.gamest.data.repository

import com.example.gamest.data.network.IgdbAgeRatingDto
import com.example.gamest.data.network.IgdbGameDto
import com.example.gamest.data.network.IgdbTimeToBeatDto
import com.example.gamest.data.network.WorkerGamesApiService
import com.example.gamest.model.GameAgeRatingUiModel
import com.example.gamest.model.GameCompanyUiModel
import com.example.gamest.model.GameDetailsUiModel
import com.example.gamest.model.GameFilter
import com.example.gamest.model.GamePage
import com.example.gamest.model.GamePlatformUiModel
import com.example.gamest.model.GameTagUiModel
import com.example.gamest.model.GameTimeToBeatUiModel
import com.example.gamest.model.GameUiModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class GamesRepository(
    private val apiService: WorkerGamesApiService,
    private val isWorkerConfigured: Boolean
) {

    suspend fun getGameDetails(gameId: Int): GameDetailsUiModel {
        checkConfiguration()
        val response = apiService.getGameDetails(gameId)
        return response.game.toGameDetailsUiModel(response.timeToBeat)
    }

    suspend fun getGenres(): List<GameTagUiModel> {
        checkConfiguration()
        return apiService.getGenres().items.map { genre ->
            GameTagUiModel(
                id = genre.id,
                name = genre.name,
                slug = genre.slug
            )
        }
    }

    suspend fun getGames(
        searchQuery: String,
        filter: GameFilter,
        page: Int
    ): GamePage {
        checkConfiguration()
        val search = searchQuery.trim().takeIf(String::isNotEmpty)
        val offset = (page - 1).coerceAtLeast(0) * PAGE_SIZE
        val maxResults = filter.maxResults

        if (maxResults != null && offset >= maxResults) {
            return GamePage(games = emptyList(), hasMore = false)
        }

        val requestLimit = maxResults
            ?.let { limit -> minOf(PAGE_SIZE, limit - offset) }
            ?: PAGE_SIZE

        val response = apiService.getGames(
            search = search,
            sort = if (search != null) "relevance" else filter.sort,
            genreId = if (search == null) filter.genreId else null,
            platformId = if (search == null) filter.platformId else null,
            platformIds = if (search == null) {
                filter.platformIds.takeIf { it.isNotEmpty() }?.joinToString(",")
            } else {
                null
            },
            developerId = if (search == null) filter.developerId else null,
            publisherId = if (search == null) filter.publisherId else null,
            ageRatingCategoryId = if (search == null) {
                filter.ageRatingCategoryId
            } else {
                null
            },
            minimumRatings = if (search == null) filter.minimumRatings else null,
            topOnly = if (search == null) filter.topOnly else null,
            offset = offset,
            limit = requestLimit
        )

        val games = response.items.map(IgdbGameDto::toGameUiModel)
        return GamePage(
            games = games,
            hasMore = response.pagination.hasMore &&
                (maxResults == null || offset + games.size < maxResults)
        )
    }

    private fun checkConfiguration() {
        if (!isWorkerConfigured) {
            throw WorkerConfigurationException(
                "WORKER_BASE_URL is missing from local.properties."
            )
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}

private fun IgdbGameDto.toGameUiModel(): GameUiModel {
    return GameUiModel(
        id = id,
        title = name,
        imageUrl = cover?.imageId?.toIgdbImageUrl("t_cover_big").orEmpty(),
        communityRating = rating ?: 0.0,
        genres = genres.map { genre -> genre.name },
        platforms = platforms.map { platform -> platform.name },
        isSaved = false,
        criticRating = aggregatedRating?.toRoundedInt()
    )
}

private fun IgdbGameDto.toGameDetailsUiModel(
    timeToBeat: IgdbTimeToBeatDto?
): GameDetailsUiModel {
    val developers = involvedCompanies
        .filter { company -> company.developer }
        .map { involved -> involved.company.toUiModel() }
        .distinctBy(GameCompanyUiModel::id)
    val publishers = involvedCompanies
        .filter { company -> company.publisher }
        .map { involved -> involved.company.toUiModel() }
        .distinctBy(GameCompanyUiModel::id)
    val screenshots = screenshots.map { screenshot ->
        screenshot.imageId.toIgdbImageUrl("t_1080p")
    }
    val coverUrl = cover?.imageId
        ?.toIgdbImageUrl("t_cover_big")
        .orEmpty()

    return GameDetailsUiModel(
        id = id,
        title = name,
        imageUrl = coverUrl,
        description = summary ?: storyline.orEmpty(),
        releaseDate = firstReleaseDate.toReleaseDate(),
        communityRating = rating ?: 0.0,
        criticRating = aggregatedRating?.toRoundedInt(),
        genres = genres.map { genre ->
            GameTagUiModel(genre.id, genre.name, genre.slug)
        },
        platforms = platforms.map { platform -> platform.name },
        platformDetails = platforms.map { platform ->
            GamePlatformUiModel(
                id = platform.id,
                name = platform.name,
                abbreviation = platform.abbreviation
            )
        },
        developers = developers,
        publishers = publishers,
        screenshots = screenshots.ifEmpty { listOfNotNull(coverUrl.takeIf(String::isNotEmpty)) },
        isSaved = false,
        ageRating = ageRatings.selectPreferredRating()?.toUiModel(),
        timeToBeat = GameTimeToBeatUiModel(
            hastilySeconds = timeToBeat?.hastily,
            normallySeconds = timeToBeat?.normally,
            completelySeconds = timeToBeat?.completely,
            submissionsCount = timeToBeat?.count ?: 0
        )
    )
}

private fun com.example.gamest.data.network.IgdbNamedReferenceDto.toUiModel() =
    GameCompanyUiModel(id = id, name = name, slug = slug)

private fun List<IgdbAgeRatingDto>.selectPreferredRating(): IgdbAgeRatingDto? {
    return firstOrNull { rating ->
        rating.organization?.name.equals("PEGI", ignoreCase = true)
    } ?: firstOrNull { rating ->
        rating.organization?.name.equals("ESRB", ignoreCase = true)
    } ?: firstOrNull()
}

private fun IgdbAgeRatingDto.toUiModel(): GameAgeRatingUiModel? {
    val category = ratingCategory ?: return null
    val organizationName = organization?.name.orEmpty()
    return GameAgeRatingUiModel(
        id = category.id,
        name = listOf(organizationName, category.rating)
            .filter(String::isNotBlank)
            .joinToString(" "),
        slug = category.rating.lowercase(Locale.ENGLISH)
    )
}

private fun String.toIgdbImageUrl(size: String): String {
    return "https://images.igdb.com/igdb/image/upload/$size/$this.jpg"
}

private fun Long?.toReleaseDate(): String {
    if (this == null) return "Unknown date"
    return Instant.ofEpochSecond(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
}

private fun Double.toRoundedInt(): Int = roundToInt()

class WorkerConfigurationException(message: String) : IllegalStateException(message)
