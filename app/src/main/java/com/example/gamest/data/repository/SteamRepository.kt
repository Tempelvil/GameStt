package com.example.gamest.data.repository

import com.example.gamest.data.network.steam.SteamApiService
import com.example.gamest.data.network.steam.SteamGameDto
import com.example.gamest.data.local.SteamGameDao
import com.example.gamest.data.local.SteamGameEntity
import com.example.gamest.data.local.SteamPlaytimeDeltaEntity
import com.example.gamest.data.local.SteamProfileEntity
import com.example.gamest.data.local.SteamProfileStatus
import com.example.gamest.data.local.SteamSyncEntity
import com.example.gamest.data.local.SteamIgdbMappingEntity
import com.example.gamest.data.local.SteamIgdbMatchStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SteamRepository {
    suspend fun checkConnection(
        profileUrl: String
    ): SteamConnectionResult

    fun observeLibrary(steamId: String): Flow<List<SteamGame>>

    suspend fun replaceLibrary(
        steamId: String,
        games: List<SteamGame>
    )

    suspend fun clearLibrary(steamId: String)

    fun observeProfiles(): Flow<List<SteamProfile>>

    fun observeSyncs(steamId: String): Flow<List<SteamSync>>

    fun observeDeltas(steamId: String): Flow<List<SteamPlaytimeDelta>>

    suspend fun saveProfileAndLibrary(result: SteamConnectionResult)

    suspend fun updateProfileStatus(steamId: String, status: String)

    suspend fun deleteProfileData(steamId: String)

    suspend fun resolveIgdbGame(appId: Int): SteamIgdbMatch
}

data class SteamIgdbMatch(
    val steamAppId: Int,
    val igdbGameId: Int?,
    val status: String
)

data class SteamProfile(
    val steamId: String,
    val profileUrl: String,
    val personaName: String,
    val avatarUrl: String?,
    val status: String,
    val createdAt: Long,
    val lastSyncAt: Long?
)

data class SteamSync(
    val syncedAt: Long,
    val totalPlaytimeMinutes: Long,
    val recentTwoWeeksMinutes: Long,
    val deltaMinutes: Long,
    val isUntrackedPeriod: Boolean
)

data class SteamPlaytimeDelta(
    val appId: Int,
    val recordedAt: Long,
    val deltaMinutes: Long,
    val totalMinutesAfterSync: Long,
    val isUntrackedPeriod: Boolean
)

data class SteamConnectionResult(
    val steamId: String,
    val personaName: String,
    val avatarUrl: String?,
    val canonicalProfileUrl: String,
    val ownedGamesCount: Int,
    val totalPlaytimeMinutes: Long,
    val recentlyPlayedCount: Int,
    val games: List<SteamGame>
)

data class SteamGame(
    val appId: Int,
    val name: String,
    val iconHash: String?,
    val totalPlaytimeMinutes: Long,
    val recentPlaytimeMinutes: Long?,
    val lastPlayedAt: Long?
)

class DefaultSteamRepository(
    private val apiService: SteamApiService,
    private val steamGameDao: SteamGameDao
) : SteamRepository {

    override suspend fun resolveIgdbGame(appId: Int): SteamIgdbMatch {
        val cached = steamGameDao.getIgdbMapping(appId)
        val now = System.currentTimeMillis()
        val cachedMaxAge = if (cached?.status == SteamIgdbMatchStatus.EXACT) {
            EXACT_MATCH_REFRESH_MILLIS
        } else {
            UNMATCHED_RETRY_MILLIS
        }
        if (cached != null && now - cached.checkedAt < cachedMaxAge) {
            return cached.toDomain()
        }

        val response = apiService.getGameMatch(appId)
        val normalizedStatus = response.status.uppercase()
        check(response.steamAppId == appId) {
            "GameShelf returned a match for a different Steam game."
        }
        check(
            normalizedStatus == SteamIgdbMatchStatus.EXACT ||
                normalizedStatus == SteamIgdbMatchStatus.UNMATCHED ||
                normalizedStatus == SteamIgdbMatchStatus.AMBIGUOUS
        ) {
            "GameShelf returned an unknown Steam match status."
        }
        check(
            normalizedStatus != SteamIgdbMatchStatus.EXACT ||
                response.igdbGameId != null
        ) {
            "GameShelf returned an incomplete Steam game match."
        }
        val mapping = SteamIgdbMappingEntity(
            steamAppId = appId,
            igdbGameId = response.igdbGameId,
            status = normalizedStatus,
            checkedAt = now
        )
        steamGameDao.insertIgdbMapping(mapping)
        return mapping.toDomain()
    }

    override fun observeProfiles(): Flow<List<SteamProfile>> {
        return steamGameDao.observeProfiles()
            .map { profiles -> profiles.map(SteamProfileEntity::toDomain) }
    }

    override fun observeSyncs(steamId: String): Flow<List<SteamSync>> {
        return steamGameDao.observeSyncs(steamId)
            .map { syncs -> syncs.map(SteamSyncEntity::toDomain) }
    }

    override fun observeDeltas(
        steamId: String
    ): Flow<List<SteamPlaytimeDelta>> {
        return steamGameDao.observeDeltas(steamId)
            .map { deltas ->
                deltas.map(SteamPlaytimeDeltaEntity::toDomain)
            }
    }

    override suspend fun saveProfileAndLibrary(
        result: SteamConnectionResult
    ) {
        val oldProfile = steamGameDao.getProfile(result.steamId)
        if (oldProfile == null && steamGameDao.getProfileCount() >= MAX_PROFILES) {
            throw SteamProfileLimitException(
                "You can save up to $MAX_PROFILES Steam profiles."
            )
        }

        val now = System.currentTimeMillis()
        val oldGames = steamGameDao.getGames(result.steamId)
            .associateBy(SteamGameEntity::appId)
        val hasTrackingGap = oldProfile != null && (
            oldProfile.status != SteamProfileStatus.LINKED ||
                oldProfile.lastSyncAt == null ||
                now - oldProfile.lastSyncAt > TRACKING_GAP_MILLIS
            )
        val deltas = if (oldProfile == null || oldGames.isEmpty()) {
            emptyList()
        } else {
            result.games.mapNotNull { game ->
                val previousMinutes = oldGames[game.appId]
                    ?.playtimeForeverMinutes ?: 0
                val delta = game.totalPlaytimeMinutes - previousMinutes
                delta.takeIf { it > 0 }?.let {
                    SteamPlaytimeDeltaEntity(
                        steamId = result.steamId,
                        appId = game.appId,
                        recordedAt = now,
                        deltaMinutes = delta,
                        totalMinutesAfterSync = game.totalPlaytimeMinutes,
                        isUntrackedPeriod = hasTrackingGap
                    )
                }
            }
        }

        steamGameDao.recordSynchronization(
            profile = SteamProfileEntity(
                steamId = result.steamId,
                profileUrl = result.canonicalProfileUrl,
                personaName = result.personaName,
                avatarUrl = result.avatarUrl,
                status = SteamProfileStatus.LINKED,
                createdAt = oldProfile?.createdAt ?: now,
                lastSyncAt = now
            ),
            games = result.games.map { game ->
                game.toEntity(result.steamId, now)
            },
            sync = SteamSyncEntity(
                steamId = result.steamId,
                syncedAt = now,
                totalPlaytimeMinutes = result.totalPlaytimeMinutes,
                recentTwoWeeksMinutes = result.games.sumOf { game ->
                    game.recentPlaytimeMinutes ?: 0
                },
                deltaMinutes = deltas.sumOf { delta -> delta.deltaMinutes },
                isUntrackedPeriod = hasTrackingGap
            ),
            deltas = deltas
        )
    }

    override suspend fun updateProfileStatus(
        steamId: String,
        status: String
    ) {
        steamGameDao.updateProfileStatus(steamId, status)
    }

    override suspend fun deleteProfileData(steamId: String) {
        steamGameDao.deleteProfileData(steamId)
    }

    override fun observeLibrary(
        steamId: String
    ): Flow<List<SteamGame>> {
        return steamGameDao.observeGames(steamId)
            .map { games -> games.map(SteamGameEntity::toSteamGame) }
    }

    override suspend fun replaceLibrary(
        steamId: String,
        games: List<SteamGame>
    ) {
        val syncedAt = System.currentTimeMillis()
        steamGameDao.replaceGames(
            steamId = steamId,
            games = games.map { game ->
                game.toEntity(steamId, syncedAt)
            }
        )
    }

    override suspend fun clearLibrary(steamId: String) {
        steamGameDao.deleteGames(steamId)
    }

    override suspend fun checkConnection(
        profileUrl: String
    ): SteamConnectionResult {
        val reference = SteamProfileUrlParser.parse(profileUrl)
        val steamId = resolveSteamId(reference)
        val player = apiService.getPlayerSummaries(
            steamIds = steamId
        ).response.players.firstOrNull()
            ?: throw SteamProfileNotFoundException(
                "Steam could not load this profile."
            )

        val ownedResponse = apiService.getOwnedGames(
            steamId = steamId
        ).response

        val ownedGamesCount = ownedResponse.gameCount
            ?: throw SteamGamesUnavailableException(
                "Steam found the profile, but its Game details are not available. Make them public in Steam privacy settings."
            )

        val recentlyPlayedResponse = apiService.getRecentlyPlayedGames(
            steamId = steamId
        ).response

        val ownedGames = ownedResponse.games
            .map(SteamGameDto::toSteamGame)
        val recentlyPlayedGames = recentlyPlayedResponse.games
            .map(SteamGameDto::toSteamGame)
        val recentlyPlayedById = recentlyPlayedGames.associateBy(
            SteamGame::appId
        )
        val libraryGames = ownedGames.map { ownedGame ->
            val recentGame = recentlyPlayedById[ownedGame.appId]
            ownedGame.copy(
                recentPlaytimeMinutes =
                    recentGame?.recentPlaytimeMinutes
                        ?: ownedGame.recentPlaytimeMinutes,
                lastPlayedAt = recentGame?.lastPlayedAt
                    ?: ownedGame.lastPlayedAt
            )
        }
            .sortedByDescending { game ->
                game.totalPlaytimeMinutes
            }

        return SteamConnectionResult(
            steamId = steamId,
            personaName = player.personaname,
            avatarUrl = player.avatarfull,
            canonicalProfileUrl = player.profileurl ?: profileUrl,
            ownedGamesCount = ownedGamesCount,
            totalPlaytimeMinutes = ownedGames.sumOf { game ->
                game.totalPlaytimeMinutes
            },
            recentlyPlayedCount = recentlyPlayedResponse.totalCount
                ?: recentlyPlayedGames.size,
            games = libraryGames
        )
    }

    private suspend fun resolveSteamId(
        reference: SteamProfileReference
    ): String {
        return when (reference.type) {
            SteamProfileReferenceType.STEAM_ID -> reference.value

            SteamProfileReferenceType.VANITY_NAME -> {
                val response = apiService.resolveVanityUrl(
                    vanityUrl = reference.value
                ).response

                response.steamid
                    ?.takeIf { steamId -> response.success == 1 }
                    ?: throw SteamProfileNotFoundException(
                        response.message
                            ?: "Steam could not find this profile."
                    )
            }
        }
    }

    private companion object {
        const val MAX_PROFILES = 4
        const val TRACKING_GAP_MILLIS = 48L * 60L * 60L * 1_000L
        const val UNMATCHED_RETRY_MILLIS = 30L * 24L * 60L * 60L * 1_000L
        const val EXACT_MATCH_REFRESH_MILLIS =
            180L * 24L * 60L * 60L * 1_000L
    }

}

private fun SteamGameDto.toSteamGame(): SteamGame {
    return SteamGame(
        appId = appid,
        name = name,
        iconHash = iconHash,
        totalPlaytimeMinutes = playtimeForever.coerceAtLeast(0),
        recentPlaytimeMinutes = playtimeTwoWeeks
            ?.coerceAtLeast(0),
        lastPlayedAt = lastPlayedAt
    )
}

private fun SteamGame.toEntity(
    steamId: String,
    syncedAt: Long
): SteamGameEntity {
    return SteamGameEntity(
        steamId = steamId,
        appId = appId,
        name = name,
        iconHash = iconHash,
        playtimeForeverMinutes = totalPlaytimeMinutes,
        playtimeTwoWeeksMinutes = recentPlaytimeMinutes,
        lastPlayedAt = lastPlayedAt,
        lastSyncedAt = syncedAt
    )
}

private fun SteamGameEntity.toSteamGame(): SteamGame {
    return SteamGame(
        appId = appId,
        name = name,
        iconHash = iconHash,
        totalPlaytimeMinutes = playtimeForeverMinutes,
        recentPlaytimeMinutes = playtimeTwoWeeksMinutes,
        lastPlayedAt = lastPlayedAt
    )
}

class SteamConfigurationException(
    message: String
) : IllegalStateException(message)

class SteamProfileNotFoundException(
    message: String
) : IllegalArgumentException(message)

class SteamGamesUnavailableException(
    message: String
) : IllegalStateException(message)

class SteamProfileLimitException(
    message: String
) : IllegalStateException(message)

private fun SteamProfileEntity.toDomain() = SteamProfile(
    steamId = steamId,
    profileUrl = profileUrl,
    personaName = personaName,
    avatarUrl = avatarUrl,
    status = status,
    createdAt = createdAt,
    lastSyncAt = lastSyncAt
)

private fun SteamSyncEntity.toDomain() = SteamSync(
    syncedAt = syncedAt,
    totalPlaytimeMinutes = totalPlaytimeMinutes,
    recentTwoWeeksMinutes = recentTwoWeeksMinutes,
    deltaMinutes = deltaMinutes,
    isUntrackedPeriod = isUntrackedPeriod
)

private fun SteamPlaytimeDeltaEntity.toDomain() = SteamPlaytimeDelta(
    appId = appId,
    recordedAt = recordedAt,
    deltaMinutes = deltaMinutes,
    totalMinutesAfterSync = totalMinutesAfterSync,
    isUntrackedPeriod = isUntrackedPeriod
)

private fun SteamIgdbMappingEntity.toDomain() = SteamIgdbMatch(
    steamAppId = steamAppId,
    igdbGameId = igdbGameId,
    status = status
)
