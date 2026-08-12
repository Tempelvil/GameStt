package com.example.gamest.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steam_syncs",
    indices = [Index(value = ["steamId", "syncedAt"])]
)
data class SteamSyncEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val steamId: String,
    val syncedAt: Long,
    val totalPlaytimeMinutes: Long,
    val recentTwoWeeksMinutes: Long,
    val deltaMinutes: Long,
    val isUntrackedPeriod: Boolean
)

@Entity(
    tableName = "steam_playtime_deltas",
    indices = [
        Index(value = ["steamId", "recordedAt"]),
        Index(value = ["steamId", "appId"])
    ]
)
data class SteamPlaytimeDeltaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val steamId: String,
    val appId: Int,
    val recordedAt: Long,
    val deltaMinutes: Long,
    val totalMinutesAfterSync: Long,
    val isUntrackedPeriod: Boolean
)
