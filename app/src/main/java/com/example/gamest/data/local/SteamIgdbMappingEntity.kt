package com.example.gamest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steam_igdb_mappings")
data class SteamIgdbMappingEntity(
    @PrimaryKey
    val steamAppId: Int,
    val igdbGameId: Int?,
    val status: String,
    val checkedAt: Long
)

object SteamIgdbMatchStatus {
    const val EXACT = "EXACT"
    const val UNMATCHED = "UNMATCHED"
    const val AMBIGUOUS = "AMBIGUOUS"
}
