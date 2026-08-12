package com.example.gamest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SteamGameDao {

    @Query("SELECT * FROM steam_profiles ORDER BY createdAt ASC")
    fun observeProfiles(): Flow<List<SteamProfileEntity>>

    @Query("SELECT * FROM steam_profiles WHERE steamId = :steamId LIMIT 1")
    suspend fun getProfile(steamId: String): SteamProfileEntity?

    @Query("SELECT COUNT(*) FROM steam_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: SteamProfileEntity)

    @Query("UPDATE steam_profiles SET status = :status WHERE steamId = :steamId")
    suspend fun updateProfileStatus(steamId: String, status: String)

    @Query(
        """
        SELECT * FROM steam_games
        WHERE steamId = :steamId
        ORDER BY playtimeForeverMinutes DESC, name COLLATE NOCASE ASC
        """
    )
    fun observeGames(steamId: String): Flow<List<SteamGameEntity>>

    @Query("SELECT * FROM steam_games WHERE steamId = :steamId")
    suspend fun getGames(steamId: String): List<SteamGameEntity>

    @Query(
        """
        SELECT * FROM steam_syncs
        WHERE steamId = :steamId
        ORDER BY syncedAt ASC
        """
    )
    fun observeSyncs(steamId: String): Flow<List<SteamSyncEntity>>

    @Query(
        """
        SELECT * FROM steam_playtime_deltas
        WHERE steamId = :steamId
        ORDER BY recordedAt ASC
        """
    )
    fun observeDeltas(steamId: String): Flow<List<SteamPlaytimeDeltaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<SteamGameEntity>)

    @Query("DELETE FROM steam_games WHERE steamId = :steamId")
    suspend fun deleteGames(steamId: String)

    @Insert
    suspend fun insertSync(sync: SteamSyncEntity)

    @Insert
    suspend fun insertDeltas(deltas: List<SteamPlaytimeDeltaEntity>)

    @Query("DELETE FROM steam_syncs WHERE steamId = :steamId")
    suspend fun deleteSyncs(steamId: String)

    @Query("DELETE FROM steam_playtime_deltas WHERE steamId = :steamId")
    suspend fun deleteDeltas(steamId: String)

    @Query("DELETE FROM steam_profiles WHERE steamId = :steamId")
    suspend fun deleteProfile(steamId: String)

    @Transaction
    suspend fun replaceGames(
        steamId: String,
        games: List<SteamGameEntity>
    ) {
        deleteGames(steamId)
        if (games.isNotEmpty()) {
            insertGames(games)
        }
    }

    @Transaction
    suspend fun recordSynchronization(
        profile: SteamProfileEntity,
        games: List<SteamGameEntity>,
        sync: SteamSyncEntity,
        deltas: List<SteamPlaytimeDeltaEntity>
    ) {
        insertProfile(profile)
        replaceGames(profile.steamId, games)
        insertSync(sync)
        if (deltas.isNotEmpty()) {
            insertDeltas(deltas)
        }
    }

    @Transaction
    suspend fun deleteProfileData(steamId: String) {
        deleteGames(steamId)
        deleteSyncs(steamId)
        deleteDeltas(steamId)
        deleteProfile(steamId)
    }
}
