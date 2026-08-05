package com.example.gamest.data.repository

import com.example.gamest.data.local.GameDao
import com.example.gamest.data.local.GameEntity
import com.example.gamest.data.local.GameStatus
import kotlinx.coroutines.flow.Flow

class DefaultLocalGamesRepository(
    private val gameDao: GameDao
) : LocalGamesRepository {

    override fun getAllGames(): Flow<List<GameEntity>> {
        return gameDao.getAllGames()
    }

    override fun getGamesByStatus(
        status: GameStatus
    ): Flow<List<GameEntity>> {
        return gameDao.getGamesByStatus(status)
    }

    override fun observeGameById(
        gameId: Int
    ): Flow<GameEntity?> {
        return gameDao.observeGameById(gameId)
    }

    override fun observeIsGameSaved(
        gameId: Int
    ): Flow<Boolean> {
        return gameDao.observeIsGameSaved(gameId)
    }

    override suspend fun getGameById(
        gameId: Int
    ): GameEntity? {
        return gameDao.getGameById(gameId)
    }

    override suspend fun saveGame(
        game: GameEntity
    ) {
        gameDao.insertGame(game)
    }

    override suspend fun deleteGame(
        game: GameEntity
    ) {
        gameDao.deleteGame(game)
    }

    override suspend fun updateStatus(
        gameId: Int,
        status: GameStatus
    ) {
        gameDao.updateStatus(
            gameId = gameId,
            status = status
        )
    }

    override suspend fun updateUserRating(
        gameId: Int,
        userRating: Int?
    ) {
        gameDao.updateUserRating(
            gameId = gameId,
            userRating = userRating
        )
    }

    override suspend fun updateHoursPlayed(
        gameId: Int,
        hoursPlayed: Int
    ) {
        gameDao.updateHoursPlayed(
            gameId = gameId,
            hoursPlayed = hoursPlayed
        )
    }

    override fun getGamesSortedByTitle(): Flow<List<GameEntity>> {
        return gameDao.getGamesSortedByTitle()
    }

    override fun getGamesSortedByUserRating(): Flow<List<GameEntity>> {
        return gameDao.getGamesSortedByUserRating()
    }

    override fun getGamesSortedByHoursPlayed(): Flow<List<GameEntity>> {
        return gameDao.getGamesSortedByHoursPlayed()
    }
}