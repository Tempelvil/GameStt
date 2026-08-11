package com.example.gamest.data
import android.content.Context
import androidx.room.Room
import com.example.gamest.BuildConfig
import com.example.gamest.data.local.GameDao
import com.example.gamest.data.local.GameDatabase
import com.example.gamest.data.network.RawgApiService
import com.example.gamest.data.network.steam.SteamApiService
import com.example.gamest.data.repository.DefaultLocalGamesRepository
import com.example.gamest.data.repository.DefaultSteamRepository
import com.example.gamest.data.repository.GamesRepository
import com.example.gamest.data.repository.LocalGamesRepository
import com.example.gamest.data.repository.SteamRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import com.example.gamest.data.offline.OfflineMode
import com.example.gamest.data.offline.OfflineRawgApiService

interface AppContainer {
    val gamesRepository: GamesRepository
    val localGamesRepository: LocalGamesRepository
    val steamRepository: SteamRepository
}


class DefaultAppContainer(
    private val context: Context
) : AppContainer {

    private val gameDatabase: GameDatabase by lazy {
        Room.databaseBuilder(
            context = context,
            klass = GameDatabase::class.java,
            name = "games_database"
        ).build()
    }
    override val localGamesRepository: LocalGamesRepository by lazy {
        DefaultLocalGamesRepository(
            gameDao = gameDatabase.gameDao()
        )
    }

    private val baseUrl = "https://api.rawg.io/api/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(
            json.asConverterFactory("application/json".toMediaType())
        )
        .build()

    private val steamRetrofit = Retrofit.Builder()
        .baseUrl("https://api.steampowered.com/")
        .client(okHttpClient)
        .addConverterFactory(
            json.asConverterFactory("application/json".toMediaType())
        )
        .build()

    private val rawgApiService: RawgApiService by lazy {

        if (OfflineMode.enabled) {
            OfflineRawgApiService()
        } else {
            retrofit.create(RawgApiService::class.java)
        }
    }


    override val gamesRepository: GamesRepository by lazy {
        GamesRepository(
            apiService = rawgApiService,
            apiKey = BuildConfig.RAWG_API_KEY
        )
    }

    private val steamApiService: SteamApiService by lazy {
        steamRetrofit.create(SteamApiService::class.java)
    }

    override val steamRepository: SteamRepository by lazy {
        DefaultSteamRepository(
            apiService = steamApiService,
            apiKey = BuildConfig.STEAM_API_KEY
        )
    }
}
