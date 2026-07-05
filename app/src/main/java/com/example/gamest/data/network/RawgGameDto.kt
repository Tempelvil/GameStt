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
    val genres: List<RawgGenreDto> = emptyList(),
    val platforms: List<RawgPlatformWrapperDto> = emptyList()
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