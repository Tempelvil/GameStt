package com.example.gamest.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawgGameDto(
    val id: Int,
    val name: String,
    @SerialName("background_image")
    val backgroundImage: String? = null,
    val rating: Double = 0.0,
    val genres: List<RawgGenreDto>? = emptyList(),
    val platforms: List<RawgPlatformWrapperDto>? = emptyList(),
    val metacritic: Int? = null
)
@Serializable
data class RawgGenreDto(
    val id: Int,
    val name: String,
    val slug: String
)
@Serializable
data class RawgPlatformWrapperDto(
    val platform: RawgPlatformDto
)
@Serializable
data class RawgPlatformDto(
    val id: Int,
    val name: String,
    val slug: String
)

@Serializable
data class RawgGameDetailsDto(
    val id: Int,
    val name: String,

    @SerialName("background_image")
    val backgroundImage: String?=null,

    @SerialName("description_raw")
    val descriptionRaw: String = "",

    val released: String? = null,
    val rating: Double = 0.0,
    val metacritic: Int? = null,

    val genres: List<RawgGenreDto> = emptyList(),
    val platforms: List<RawgPlatformWrapperDto>? = emptyList(),
    val developers: List<RawgDeveloperDto> = emptyList(),
    val publishers: List<RawgPublisherDto> = emptyList(),
    val playtime: Int = 0,

    @SerialName("esrb_rating")
    val esrbRating: RawgEsrbRatingDto? = null
)
@Serializable
data class RawgDeveloperDto(
    val id: Int,
    val name: String,
    val slug: String
)

@Serializable
data class RawgPublisherDto(
    val id: Int,
    val name: String,
    val slug: String
)
@Serializable
data class RawgEsrbRatingDto(
    val id: Int,
    val name: String,
    val slug: String
)