package com.example.gamest.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawgGamesResponseDto (
        @SerialName("results")
        val listGame: List<RawgGameDto>
        )