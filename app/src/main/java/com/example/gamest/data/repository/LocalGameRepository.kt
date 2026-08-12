package com.example.gamest.data.repository


import com.example.gamest.data.local.GameEntity
import com.example.gamest.data.local.GameSort
import com.example.gamest.data.local.GameStatus
import kotlinx.coroutines.flow.Flow

interface LocalGamesRepository {

    fun getAllGames(): Flow<List<GameEntity>>

    fun getGamesByStatus(
        status: GameStatus
    ): Flow<List<GameEntity>>

    fun observeIsGameSaved(
        gameId: Int
    ): Flow<Boolean>

    fun observeSavedGameIds(): Flow<Set<Int>>

    fun observeGameById(
        gameId: Int
    ): Flow<GameEntity?>

    suspend fun getGameById(
        gameId: Int
    ): GameEntity?

    suspend fun saveGame(
        game: GameEntity
    )

    suspend fun deleteGame(
        game: GameEntity
    )

    suspend fun updateStatus(
        gameId: Int,
        status: GameStatus
    )

    suspend fun updateUserRating(
        gameId: Int,
        userRating: Int?
    )

    suspend fun updateHoursPlayed(
        gameId: Int,
        hoursPlayed: Int
    )

    fun getGamesSortedByTitle(): Flow<List<GameEntity>>

    fun getGamesSortedByUserRating(): Flow<List<GameEntity>>

    fun getGamesSortedByHoursPlayed(): Flow<List<GameEntity>>

    fun observeGames(
        status: GameStatus?,
        sort: GameSort
    ): Flow<List<GameEntity>>

    suspend fun updatePersonalData(
        gameId: Int,
        status: GameStatus,
        userRating: Int?,
        hoursPlayed: Int
    )
}
