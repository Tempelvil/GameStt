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
        SteamPlaytimeDeltaEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(GameConverters::class)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    abstract fun steamGameDao(): SteamGameDao
}
