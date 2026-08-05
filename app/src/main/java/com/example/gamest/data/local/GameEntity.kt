package com.example.gamest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_games")
data class GameEntity(
    @PrimaryKey
    val id: Int,

    val title: String,
    val imageUrl: String?,
    val description: String,
    val releaseDate: String,

    val ratingRawg: Double = 0.0,
    val metacritic: Int?,
    val playtime: Int,

    val genres: List<StoredTag>,
    val platforms: List<String>,
    val developers: List<StoredCompany>,
    val publishers: List<StoredCompany>,
    val screenshots: List<String>,
    val ageRating: StoredAgeRating?,

    // Пользовательские данные
    val userRating: Int? = null,
    val status: GameStatus = GameStatus.PLAYING,
    val hoursPlayed: Int = 0,

    val savedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)