package com.example.gamest.data.network

import kotlinx.serialization.json.Json

object RawgNetWork {
    private const val BASE_URL = "https://api.rawg.io/api/"

    private val json = Json {
        ignoreUnknownKeys = true
    }
}