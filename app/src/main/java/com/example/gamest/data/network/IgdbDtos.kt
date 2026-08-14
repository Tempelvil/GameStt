package com.example.gamest.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IgdbGamesPageDto(
    val items: List<IgdbGameDto> = emptyList(),
    val pagination: IgdbPaginationDto = IgdbPaginationDto()
)

@Serializable
data class IgdbPaginationDto(
    val limit: Int = 20,
    val offset: Int = 0,
    val returned: Int = 0,
    val hasMore: Boolean = false
)

@Serializable
data class IgdbGameDetailsResponseDto(
    val game: IgdbGameDto,
    val timeToBeat: IgdbTimeToBeatDto? = null
)

@Serializable
data class IgdbGenresResponseDto(
    val items: List<IgdbNamedReferenceDto> = emptyList()
)

@Serializable
data class IgdbGameDto(
    val id: Int,
    val name: String,
    val slug: String = "",
    val summary: String? = null,
    val storyline: String? = null,
    @SerialName("first_release_date")
    val firstReleaseDate: Long? = null,
    val rating: Double? = null,
    @SerialName("rating_count")
    val ratingCount: Int = 0,
    @SerialName("aggregated_rating")
    val aggregatedRating: Double? = null,
    @SerialName("aggregated_rating_count")
    val aggregatedRatingCount: Int = 0,
    val cover: IgdbImageDto? = null,
    val genres: List<IgdbNamedReferenceDto> = emptyList(),
    val platforms: List<IgdbPlatformDto> = emptyList(),
    val screenshots: List<IgdbImageDto> = emptyList(),
    @SerialName("involved_companies")
    val involvedCompanies: List<IgdbInvolvedCompanyDto> = emptyList(),
    @SerialName("age_ratings")
    val ageRatings: List<IgdbAgeRatingDto> = emptyList()
)

@Serializable
data class IgdbNamedReferenceDto(
    val id: Int,
    val name: String,
    val slug: String = ""
)

@Serializable
data class IgdbPlatformDto(
    val id: Int,
    val name: String,
    val abbreviation: String? = null,
    val slug: String = ""
)

@Serializable
data class IgdbImageDto(
    val id: Int,
    @SerialName("image_id")
    val imageId: String,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class IgdbInvolvedCompanyDto(
    val id: Int,
    val developer: Boolean = false,
    val publisher: Boolean = false,
    val company: IgdbNamedReferenceDto
)

@Serializable
data class IgdbAgeRatingDto(
    val id: Int,
    val organization: IgdbAgeRatingOrganizationDto? = null,
    @SerialName("rating_category")
    val ratingCategory: IgdbAgeRatingCategoryDto? = null
)

@Serializable
data class IgdbAgeRatingOrganizationDto(
    val id: Int,
    val name: String
)

@Serializable
data class IgdbAgeRatingCategoryDto(
    val id: Int,
    val rating: String
)

@Serializable
data class IgdbTimeToBeatDto(
    @SerialName("game_id")
    val gameId: Int,
    val hastily: Int? = null,
    val normally: Int? = null,
    val completely: Int? = null,
    val count: Int = 0
)
