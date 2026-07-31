package com.example.gamest.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawgScreenshotsDto (
        @SerialName("results")
        val screenshots: List<RawgScreenshotDto> = emptyList()
        )
@Serializable
data class RawgScreenshotDto(
    val id: Int,
    val image: String
)