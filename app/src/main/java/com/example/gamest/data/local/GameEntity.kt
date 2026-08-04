package com.example.gamest.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_games")
data class GameEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val imageUrl: String?,

    val userRating: Int? = null,
    val ratingRawg: Double = 0.0,
    val metacritic: Int?,

    val primaryGenre: String?,
    val primaryPlatform: String?,
    val status: GameStatus = GameStatus.PLAYING,
    val hoursPlayed: Int = 0,
    val savedAt: Long = System.currentTimeMillis()

)