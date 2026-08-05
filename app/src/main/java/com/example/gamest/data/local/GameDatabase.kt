package com.example.gamest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(GameConverters::class)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
}