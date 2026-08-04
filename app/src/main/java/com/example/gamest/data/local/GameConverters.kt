package com.example.gamest.data.local

import androidx.room.TypeConverter
import androidx.room.TypeConverters

class GameConverters (){
    @TypeConverter
    fun fromGameToStatus(status: GameStatus): String{
        return status.name
    }
    @TypeConverter
    fun toGameFromStatus(value: String): GameStatus{
        return GameStatus.valueOf(value)
    }
}