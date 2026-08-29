package com.example.gamest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        GameEntity::class,
        SteamGameEntity::class,
        SteamProfileEntity::class,
        SteamSyncEntity::class,
        SteamPlaytimeDeltaEntity::class,
        SteamIgdbMappingEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(GameConverters::class)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    abstract fun steamGameDao(): SteamGameDao
}
