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
    fun observeIsGameSaved(gameId: Int): Flow<Boolean>

    @Query("SELECT * FROM saved_games WHERE id = :gameId LIMIT 1")
    fun observeGameById(gameId: Int): Flow<GameEntity?>

    @Query("""
        SELECT * FROM saved_games
        ORDER BY savedAt DESC
    """)
    fun getAllGames():Flow<List<GameEntity>>

    @Query("""
        SELECT * FROM saved_games
        WHERE status = :status
        ORDER BY savedAt Desc
    """)
    fun getGamesByStatus(status: GameStatus):Flow<List<GameEntity>>
    @Query("""
        UPDATE saved_games
        SET status = :status
        WHERE id = :gameId
    """)
    suspend fun updateStatus(
        status: GameStatus,
        gameId: Int
    )
    @Query("""
        UPDATE saved_games
        SET userRating =:userRating
        WHERE id =:gameId
    """)
    suspend fun updateUserRating(
        userRating: Int?,
        gameId: Int
    )
    @Query(
        """
    UPDATE saved_games
    SET hoursPlayed = :hoursPlayed
    WHERE id = :gameId
    """
    )
    suspend fun updateHoursPlayed(
        gameId: Int,
        hoursPlayed: Int
    )
    @Query(
        """
    SELECT * FROM saved_games
    ORDER BY title COLLATE NOCASE ASC
    """
    )
    fun getGamesSortedByTitle(): Flow<List<GameEntity>>

    @Query(
        """
    SELECT * FROM saved_games
    ORDER BY userRating DESC
    """
    )
    fun getGamesSortedByUserRating(): Flow<List<GameEntity>>

    @Query(
        """
    SELECT * FROM saved_games
    ORDER BY hoursPlayed DESC
    """
    )
    fun getGamesSortedByHoursPlayed(): Flow<List<GameEntity>>

}