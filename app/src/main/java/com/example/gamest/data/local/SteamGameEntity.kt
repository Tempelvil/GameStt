package com.example.gamest.data.local

import androidx.room.Entity

@Entity(
    tableName = "steam_games",
    primaryKeys = ["steamId", "appId"]
)
data class SteamGameEntity(
    val steamId: String,
    val appId: Int,
    val name: String,
    val iconHash: String?,
    val playtimeForeverMinutes: Long,
    val playtimeTwoWeeksMinutes: Long?,
    val lastPlayedAt: Long?,
    val lastSyncedAt: Long
)
