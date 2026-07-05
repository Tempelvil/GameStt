package com.example.gamest.data

import com.example.gamest.BuildConfig
import com.example.gamest.data.network.RawgApiService
import com.example.gamest.data.repository.GamesRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {
    val gamesRepository: GamesRepository
}

class DefaultAppContainer : AppContainer {

    private val baseUrl = "https://api.rawg.io/api/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(
            json.asConverterFactory("application/json".toMediaType())
        )
        .build()

    private val rawgApiService: RawgApiService by lazy {
        retrofit.create(RawgApiService::class.java)
    }

    override val gamesRepository: GamesRepository by lazy {
        GamesRepository(
            apiService = rawgApiService,
            apiKey = BuildConfig.RAWG_API_KEY
        )
    }
}