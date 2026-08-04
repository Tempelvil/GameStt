package com.example.gamest.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("SELECT * FROM saved_games WHERE id = :gameId LIMIT 1")
    suspend fun getGameById(gameId: Int): GameEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM saved_games WHERE id = :gameId)")
    fun observeGameSaved(gameId: Int): Flow<Boolean>

    @Query("Select*From saved_games Order By savedAt DESC")
    fun getGamesSortedOnDate(): Flow<List<GameEntity>>

    @Query("""
        SELECT * FROM saved_games
        WHERE status = :status
        ORDER BY savedAt Desc
    """)
    fun getGameByStatus(status: GameStatus):Flow<List<GameEntity>>
    @Query("""
        UPDATE saved_games
        SET status = :status
        WHERE id = :gameId
    """)
    suspend fun updateStatus(
        status: GameStatus,
        gameId: Int
    )
}