package com.example.gamest.data.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "saved_games")
data class GameEntity(
    @PrimaryKey
    val id: Int,

    val title: String,
    val imageUrl: String?,
    val description: String,
    val releaseDate: String,

    val communityRating: Double = 0.0,
    val criticRating: Int?,
    val hastilySeconds: Int?,
    val normallySeconds: Int?,
    val completelySeconds: Int?,
    val timeToBeatSubmissions: Int = 0,

    val genres: List<StoredTag>,
    val platforms: List<String>,
    @ColumnInfo(defaultValue = "'[]'")
    val platformDetails: List<StoredPlatform> = emptyList(),
    val developers: List<StoredCompany>,
    val publishers: List<StoredCompany>,
    val screenshots: List<String>,
    val ageRating: StoredAgeRating?,

    // Пользовательские данные
    val userRating: Int? = null,
    val status: GameStatus = GameStatus.PLAYING,
    val completionStyle: CompletionStyle? = null,
    val hoursPlayed: Int = 0,

    val savedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
