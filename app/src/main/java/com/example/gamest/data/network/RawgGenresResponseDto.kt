package com.example.gamest.data.network
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawgGenresResponseDto(
    @SerialName("results")
    val genres: List<RawgGenreDto> = emptyList()
)