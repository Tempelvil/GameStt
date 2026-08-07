package com.example.gamest.data.offline

import com.example.gamest.data.network.RawgApiService
import com.example.gamest.data.network.RawgGameDetailsDto
import com.example.gamest.data.network.RawgGamesResponseDto
import com.example.gamest.data.network.RawgGenresResponseDto
import com.example.gamest.data.network.RawgScreenshotsDto
import com.example.gamest.data.network.RawgScreenshotDto

class OfflineRawgApiService : RawgApiService {

    override suspend fun getGames(
        apiKey: String,
        page: Int,
        pageSize: Int,
        search: String?,
        genres: String?,
        developers: String?,
        publishers: String?,
        esrbRating: String?,
        platforms: String?,
        ordering: String?
    ): RawgGamesResponseDto {

        var games = OfflineGamesData.games

        if (!search.isNullOrBlank()) {
            games = games.filter { game ->
                game.details.name.contains(
                    other = search,
                    ignoreCase = true
                )
            }
        }

        if (!genres.isNullOrBlank()) {
            val requestedGenres = genres
                .split(",")
                .toSet()

            games = games.filter { game ->
                game.details.genres.any { genre ->
                    genre.slug in requestedGenres ||
                            genre.id.toString() in requestedGenres
                }
            }
        }

        if (!platforms.isNullOrBlank()) {
            val requestedPlatforms = platforms
                .split(",")
                .toSet()

            games = games.filter { game ->
                game.details.platforms
                    ?.any { wrapper ->
                        wrapper.platform.id.toString() in requestedPlatforms ||
                                wrapper.platform.slug in requestedPlatforms
                    }
                    ?: false
            }
        }

        if (!developers.isNullOrBlank()) {
            val requestedDevelopers = developers
                .split(",")
                .toSet()

            games = games.filter { game ->
                game.details.developers.any { developer ->
                    developer.id.toString() in requestedDevelopers
                }
            }
        }

        if (!publishers.isNullOrBlank()) {
            val requestedPublishers = publishers
                .split(",")
                .toSet()

            games = games.filter { game ->
                game.details.publishers.any { publisher ->
                    publisher.id.toString() in requestedPublishers
                }
            }
        }

        if (!esrbRating.isNullOrBlank()) {
            val requestedRatings = esrbRating
                .split(",")
                .toSet()

            games = games.filter { game ->
                val rating = game.details.esrbRating

                rating != null &&
                        (
                                rating.id.toString() in requestedRatings ||
                                        rating.slug in requestedRatings
                                )
            }
        }

        games = when (ordering) {
            "-rating" -> games.sortedByDescending {
                it.details.rating
            }

            "rating" -> games.sortedBy {
                it.details.rating
            }

            "-metacritic" -> games.sortedByDescending {
                it.details.metacritic ?: -1
            }

            "metacritic" -> games.sortedBy {
                it.details.metacritic ?: Int.MAX_VALUE
            }

            else -> games
        }

        val fromIndex =
            ((page - 1) * pageSize)
                .coerceAtLeast(0)

        val pageGames = games
            .drop(fromIndex)
            .take(pageSize)

        return RawgGamesResponseDto(
            listGame = pageGames.map { game ->
                game.toGameDto()
            }
        )
    }

    override suspend fun getGamesDetails(
        gameId: Int,
        apiKey: String
    ): RawgGameDetailsDto {

        return OfflineGamesData.games
            .firstOrNull { game ->
                game.details.id == gameId
            }
            ?.details
            ?: error(
                "Offline game with id=$gameId was not found"
            )
    }

    override suspend fun getScreenshots(
        gameId: Int,
        apiKey: String,
        pageSize: Int
    ): RawgScreenshotsDto {

        val game = OfflineGamesData.games
            .firstOrNull { game ->
                game.details.id == gameId
            }
            ?: error(
                "Offline game with id=$gameId was not found"
            )

        return RawgScreenshotsDto(
            screenshots = game.screenshots
                .take(pageSize)
                .mapIndexed { index, image ->
                    RawgScreenshotDto(
                        id = gameId * 100 + index,
                        image = image
                    )
                }
        )
    }

    override suspend fun getGenres(
        apiKey: String,
        pageSize: Int
    ): RawgGenresResponseDto {

        return RawgGenresResponseDto(
            genres = OfflineGamesData.genres
                .take(pageSize)
        )
    }
}