package com.example.gamest.data.mapper

import com.example.gamest.data.local.GameEntity
import com.example.gamest.data.local.GameStatus
import com.example.gamest.data.local.StoredAgeRating
import com.example.gamest.data.local.StoredCompany
import com.example.gamest.data.local.StoredTag
import com.example.gamest.model.GameAgeRatingUiModel
import com.example.gamest.model.GameCompanyUiModel
import com.example.gamest.model.GameDetailsUiModel
import com.example.gamest.model.GameTagUiModel

fun GameDetailsUiModel.toGameEntity(
    status: GameStatus,
    userRating: Int?,
    hoursPlayed: Int
): GameEntity {
    return GameEntity(
        id = id,
        title = title,
        imageUrl = imageUrl,
        description = description,
        releaseDate = releaseDate,

        ratingRawg = rating,
        metacritic = metacritic,
        playtime = playtime,

        genres = genres.map { genre ->
            StoredTag(
                id = genre.id,
                name = genre.name,
                slug = genre.slug
            )
        },

        platforms = platforms,

        developers = developers.map { developer ->
            StoredCompany(
                id = developer.id,
                name = developer.name,
                slug = developer.slug
            )
        },

        publishers = publishers.map { publisher ->
            StoredCompany(
                id = publisher.id,
                name = publisher.name,
                slug = publisher.slug
            )
        },

        screenshots = screenshots,

        ageRating = ageRating?.let { rating ->
            StoredAgeRating(
                id = rating.id,
                name = rating.name,
                slug = rating.slug
            )
        },

        status = status,
        userRating = userRating,
        hoursPlayed = hoursPlayed
    )
}

fun GameEntity.toGameDetailsUiModel(): GameDetailsUiModel {
    return GameDetailsUiModel(
        id = id,
        title = title,
        imageUrl = imageUrl.orEmpty(),
        description = description,
        releaseDate = releaseDate,

        rating = ratingRawg,
        metacritic = metacritic,
        playtime = playtime,

        genres = genres.map { genre ->
            GameTagUiModel(
                id = genre.id,
                name = genre.name,
                slug = genre.slug
            )
        },

        platforms = platforms,

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

        screenshots = screenshots,

        ageRating = ageRating?.let { rating ->
            GameAgeRatingUiModel(
                id = rating.id,
                name = rating.name,
                slug = rating.slug
            )
        },

        isSaved = true
    )
}