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

    @Query(
        """
    SELECT * FROM saved_games
    WHERE (:status IS NULL OR status = :status)
    ORDER BY
        CASE
            WHEN :sort = 'TITLE'
            THEN title
        END COLLATE NOCASE ASC,

        CASE
            WHEN :sort = 'USER_RATING'
            THEN userRating
        END DESC,

        CASE
            WHEN :sort = 'HOURS_PLAYED'
            THEN hoursPlayed
        END DESC,

        CASE
            WHEN :sort = 'RECENTLY_ADDED'
            THEN savedAt
        END DESC
    """
    )
    fun observeGames(
        status: GameStatus?,
        sort: String
    ): Flow<List<GameEntity>>

    @Query(
        """
    UPDATE saved_games
    SET status = :status,
        userRating = :userRating,
        hoursPlayed = :hoursPlayed,
        updatedAt = :updatedAt
    WHERE id = :gameId
    """
    )
    suspend fun updatePersonalData(
        gameId: Int,
        status: GameStatus,
        userRating: Int?,
        hoursPlayed: Int,
        updatedAt: Long
    )
}