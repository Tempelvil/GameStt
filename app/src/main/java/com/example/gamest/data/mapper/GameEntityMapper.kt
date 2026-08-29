package com.example.gamest.data.mapper

import com.example.gamest.data.local.GameEntity
import com.example.gamest.data.local.CompletionStyle
import com.example.gamest.data.local.GameStatus
import com.example.gamest.data.local.StoredAgeRating
import com.example.gamest.data.local.StoredCompany
import com.example.gamest.data.local.StoredTag
import com.example.gamest.data.local.StoredPlatform
import com.example.gamest.model.GameAgeRatingUiModel
import com.example.gamest.model.GameCompanyUiModel
import com.example.gamest.model.GameDetailsUiModel
import com.example.gamest.model.GameTagUiModel
import com.example.gamest.model.GamePlatformUiModel

fun GameDetailsUiModel.toGameEntity(
    status: GameStatus,
    userRating: Int?,
    completionStyle: CompletionStyle?,
    hoursPlayed: Int
): GameEntity {
    return GameEntity(
        id = id,
        title = title,
        imageUrl = imageUrl,
        description = description,
        releaseDate = releaseDate,

        communityRating = communityRating,
        criticRating = criticRating,
        hastilySeconds = timeToBeat.hastilySeconds,
        normallySeconds = timeToBeat.normallySeconds,
        completelySeconds = timeToBeat.completelySeconds,
        timeToBeatSubmissions = timeToBeat.submissionsCount,

        genres = genres.map { genre ->
            StoredTag(
                id = genre.id,
                name = genre.name,
                slug = genre.slug
            )
        },

        platforms = platforms,
        platformDetails = platformDetails.map { platform ->
            StoredPlatform(
                id = platform.id,
                name = platform.name,
                abbreviation = platform.abbreviation
            )
        },

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
        completionStyle = completionStyle,
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

        communityRating = communityRating,
        criticRating = criticRating,

        genres = genres.map { genre ->
            GameTagUiModel(
                id = genre.id,
                name = genre.name,
                slug = genre.slug
            )
        },

        platforms = platforms,
        platformDetails = platformDetails.map { platform ->
            GamePlatformUiModel(
                id = platform.id,
                name = platform.name,
                abbreviation = platform.abbreviation
            )
        },

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

        timeToBeat = com.example.gamest.model.GameTimeToBeatUiModel(
            hastilySeconds = hastilySeconds,
            normallySeconds = normallySeconds,
            completelySeconds = completelySeconds,
            submissionsCount = timeToBeatSubmissions
        ),

        isSaved = true
    )
}
