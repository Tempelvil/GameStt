package com.example.gamest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steam_profiles")
data class SteamProfileEntity(
    @PrimaryKey
    val steamId: String,
    val profileUrl: String,
    val personaName: String,
    val avatarUrl: String?,
    val status: String,
    val createdAt: Long,
    val lastSyncAt: Long?
)

object SteamProfileStatus {
    const val LINKED = "LINKED"
    const val PAUSED = "PAUSED"
    const val UNLINKED = "UNLINKED"
}
