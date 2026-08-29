package com.example.gamest.data.repository

import com.example.gamest.data.network.steam.SteamApiService
import com.example.gamest.data.network.steam.SteamGameDto
import com.example.gamest.data.network.steam.SteamOwnedGamesPayloadDto
import com.example.gamest.data.network.steam.SteamOwnedGamesResponseDto
import com.example.gamest.data.network.steam.SteamPlayerDto
import com.example.gamest.data.network.steam.SteamPlayerSummariesPayloadDto
import com.example.gamest.data.network.steam.SteamPlayerSummariesResponseDto
import com.example.gamest.data.network.steam.SteamRecentlyPlayedPayloadDto
import com.example.gamest.data.network.steam.SteamRecentlyPlayedResponseDto
import com.example.gamest.data.network.steam.SteamVanityPayloadDto
import com.example.gamest.data.network.steam.SteamVanityResponseDto
import com.example.gamest.data.network.steam.SteamGameMatchDto
import com.example.gamest.data.local.SteamGameDao
import com.example.gamest.data.local.SteamGameEntity
import com.example.gamest.data.local.SteamPlaytimeDeltaEntity
import com.example.gamest.data.local.SteamProfileEntity
import com.example.gamest.data.local.SteamProfileStatus
import com.example.gamest.data.local.SteamSyncEntity
import com.example.gamest.data.local.SteamIgdbMappingEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

class SteamRepositoryTest {

    @Test
    fun checkConnection_returnsProfileAndPlaytime() = runBlocking {
        val repository = DefaultSteamRepository(
            apiService = FakeSteamApiService(),
            steamGameDao = FakeSteamGameDao()
        )

        val result = repository.checkConnection(
            "https://steamcommunity.com/id/player/"
        )

        assertEquals("76561198000000000", result.steamId)
        assertEquals("Player One", result.personaName)
        assertEquals("https://example.com/avatar.jpg", result.avatarUrl)
        assertEquals(2, result.ownedGamesCount)
        assertEquals(210L, result.totalPlaytimeMinutes)
        assertEquals(1, result.recentlyPlayedCount)
        assertEquals(2, result.games.size)
        assertEquals(
            20L,
            result.games.first { game -> game.appId == 2 }.recentPlaytimeMinutes
        )
    }

    @Test
    fun synchronization_usesFirstImportAsBaselineAndRecordsLaterGrowth() = runBlocking {
        val dao = FakeSteamGameDao()
        val repository = DefaultSteamRepository(
            apiService = FakeSteamApiService(),
            steamGameDao = dao
        )
        val first = repository.checkConnection(
            "https://steamcommunity.com/id/player/"
        )
        repository.saveProfileAndLibrary(first)
        repository.saveProfileAndLibrary(
            first.copy(
                totalPlaytimeMinutes = 240,
                games = first.games.map { game ->
                    if (game.appId == 2) {
                        game.copy(totalPlaytimeMinutes = 150)
                    } else game
                }
            )
        )

        assertEquals(2, dao.syncs.size)
        assertEquals(0L, dao.syncs.first().deltaMinutes)
        assertEquals(30L, dao.syncs.last().deltaMinutes)
        assertEquals(30L, dao.deltas.single().deltaMinutes)
    }

    @Test
    fun unlinkingProfile_keepsLibraryAndHistory() = runBlocking {
        val dao = FakeSteamGameDao()
        val repository = DefaultSteamRepository(
            apiService = FakeSteamApiService(),
            steamGameDao = dao
        )
        repository.saveProfileAndLibrary(
            repository.checkConnection("https://steamcommunity.com/id/player/")
        )

        repository.updateProfileStatus(
            "76561198000000000",
            SteamProfileStatus.UNLINKED
        )

        assertEquals(2, dao.games.size)
        assertEquals(1, dao.syncs.size)
        assertEquals(SteamProfileStatus.UNLINKED, dao.profiles.single().status)
    }

    @Test
    fun resolveIgdbGame_cachesExactSteamMapping() = runBlocking {
        val dao = FakeSteamGameDao()
        val api = FakeSteamApiService()
        val repository = DefaultSteamRepository(api, dao)

        val first = repository.resolveIgdbGame(1245620)
        val second = repository.resolveIgdbGame(1245620)

        assertEquals(119133, first.igdbGameId)
        assertEquals(first, second)
        assertEquals(1, api.matchRequests)
    }
}

private class FakeSteamApiService : SteamApiService {
    var matchRequests = 0

    override suspend fun getGameMatch(appId: Int): SteamGameMatchDto {
        matchRequests += 1
        return SteamGameMatchDto(
            steamAppId = appId,
            igdbGameId = 119133,
            status = "exact"
        )
    }

    override suspend fun getPlayerSummaries(
        steamIds: String
    ) = SteamPlayerSummariesResponseDto(
        response = SteamPlayerSummariesPayloadDto(
            players = listOf(
                SteamPlayerDto(
                    steamid = steamIds,
                    personaname = "Player One",
                    profileurl =
                        "https://steamcommunity.com/profiles/$steamIds/",
                    avatarfull = "https://example.com/avatar.jpg"
                )
            )
        )
    )

    override suspend fun getOwnedGames(
        steamId: String
    ) = SteamOwnedGamesResponseDto(
        response = SteamOwnedGamesPayloadDto(
            gameCount = 2,
            games = listOf(
                SteamGameDto(
                    appid = 1,
                    name = "First game",
                    playtimeForever = 90
                ),
                SteamGameDto(
                    appid = 2,
                    name = "Second game",
                    playtimeForever = 120
                )
            )
        )
    )

    override suspend fun getRecentlyPlayedGames(
        steamId: String
    ) = SteamRecentlyPlayedResponseDto(
        response = SteamRecentlyPlayedPayloadDto(
            totalCount = 1,
            games = listOf(
                SteamGameDto(
                    appid = 2,
                    name = "Second game",
                    playtimeForever = 120,
                    playtimeTwoWeeks = 20
                )
            )
        )
    )

    override suspend fun resolveVanityUrl(
        vanityUrl: String
    ) = SteamVanityResponseDto(
        response = SteamVanityPayloadDto(
            steamid = "76561198000000000",
            success = 1
        )
    )
}

private class FakeSteamGameDao : SteamGameDao {
    val profiles = mutableListOf<SteamProfileEntity>()
    val games = mutableListOf<SteamGameEntity>()
    val syncs = mutableListOf<SteamSyncEntity>()
    val deltas = mutableListOf<SteamPlaytimeDeltaEntity>()
    val mappings = mutableListOf<SteamIgdbMappingEntity>()

    override fun observeProfiles(): Flow<List<SteamProfileEntity>> =
        flowOf(profiles)

    override suspend fun getProfile(steamId: String): SteamProfileEntity? =
        profiles.firstOrNull { it.steamId == steamId }

    override suspend fun getProfileCount(): Int = profiles.size

    override suspend fun insertProfile(profile: SteamProfileEntity) {
        profiles.removeAll { it.steamId == profile.steamId }
        profiles += profile
    }

    override suspend fun updateProfileStatus(steamId: String, status: String) {
        val profile = getProfile(steamId) ?: return
        insertProfile(profile.copy(status = status))
    }

    override fun observeGames(
        steamId: String
    ): Flow<List<SteamGameEntity>> = flowOf(
        games.filter { it.steamId == steamId }
    )

    override suspend fun getGames(steamId: String): List<SteamGameEntity> =
        games.filter { it.steamId == steamId }

    override suspend fun getIgdbMapping(appId: Int): SteamIgdbMappingEntity? =
        mappings.firstOrNull { it.steamAppId == appId }

    override suspend fun insertIgdbMapping(mapping: SteamIgdbMappingEntity) {
        mappings.removeAll { it.steamAppId == mapping.steamAppId }
        mappings += mapping
    }

    override fun observeSyncs(steamId: String): Flow<List<SteamSyncEntity>> =
        flowOf(syncs.filter { it.steamId == steamId })

    override fun observeDeltas(
        steamId: String
    ): Flow<List<SteamPlaytimeDeltaEntity>> =
        flowOf(deltas.filter { it.steamId == steamId })

    override suspend fun insertGames(games: List<SteamGameEntity>) {
        this.games += games
    }

    override suspend fun deleteGames(steamId: String) {
        games.removeAll { it.steamId == steamId }
    }

    override suspend fun insertSync(sync: SteamSyncEntity) {
        syncs += sync.copy(id = (syncs.size + 1).toLong())
    }

    override suspend fun insertDeltas(deltas: List<SteamPlaytimeDeltaEntity>) {
        this.deltas += deltas
    }

    override suspend fun deleteSyncs(steamId: String) {
        syncs.removeAll { it.steamId == steamId }
    }

    override suspend fun deleteDeltas(steamId: String) {
        deltas.removeAll { it.steamId == steamId }
    }

    override suspend fun deleteProfile(steamId: String) {
        profiles.removeAll { it.steamId == steamId }
    }
}
