package com.example.gamest.data.network

import kotlinx.serialization.Serializable

@Serializable
data class RawgGamesResponseDto (
        val listGame: List<RawgGameDto>
        )