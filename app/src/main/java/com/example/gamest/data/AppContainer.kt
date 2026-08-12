package com.example.gamest.data
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gamest.BuildConfig
import com.example.gamest.data.local.GameDao
import com.example.gamest.data.local.GameDatabase
import com.example.gamest.data.local.preferences.SteamConnectionPreferences
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

    val steamConnectionPreferences: SteamConnectionPreferences
}


class DefaultAppContainer(
    private val context: Context
) : AppContainer {

    private val gameDatabase: GameDatabase by lazy {
        Room.databaseBuilder(
            context = context,
            klass = GameDatabase::class.java,
            name = "games_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
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
            apiKey = BuildConfig.STEAM_API_KEY,
            steamGameDao = gameDatabase.steamGameDao()
        )
    }
    override val steamConnectionPreferences: SteamConnectionPreferences by lazy{
        SteamConnectionPreferences(context)
    }

    private companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS steam_games (
                        steamId TEXT NOT NULL,
                        appId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        iconHash TEXT,
                        playtimeForeverMinutes INTEGER NOT NULL,
                        playtimeTwoWeeksMinutes INTEGER,
                        lastPlayedAt INTEGER,
                        lastSyncedAt INTEGER NOT NULL,
                        PRIMARY KEY(steamId, appId)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS steam_profiles (
                        steamId TEXT NOT NULL PRIMARY KEY,
                        profileUrl TEXT NOT NULL,
                        personaName TEXT NOT NULL,
                        avatarUrl TEXT,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        lastSyncAt INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS steam_syncs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        steamId TEXT NOT NULL,
                        syncedAt INTEGER NOT NULL,
                        totalPlaytimeMinutes INTEGER NOT NULL,
                        recentTwoWeeksMinutes INTEGER NOT NULL,
                        deltaMinutes INTEGER NOT NULL,
                        isUntrackedPeriod INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_steam_syncs_steamId_syncedAt
                    ON steam_syncs(steamId, syncedAt)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS steam_playtime_deltas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        steamId TEXT NOT NULL,
                        appId INTEGER NOT NULL,
                        recordedAt INTEGER NOT NULL,
                        deltaMinutes INTEGER NOT NULL,
                        totalMinutesAfterSync INTEGER NOT NULL,
                        isUntrackedPeriod INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_steam_playtime_deltas_steamId_recordedAt
                    ON steam_playtime_deltas(steamId, recordedAt)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_steam_playtime_deltas_steamId_appId
                    ON steam_playtime_deltas(steamId, appId)
                    """.trimIndent()
                )
            }
        }
    }

}
