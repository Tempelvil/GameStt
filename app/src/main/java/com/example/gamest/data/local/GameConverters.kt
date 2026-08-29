package com.example.gamest.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameConverters {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromGameStatus(status: GameStatus): String {
        return status.name
    }

    @TypeConverter
    fun toGameStatus(value: String): GameStatus {
        return GameStatus.valueOf(value)
    }

    @TypeConverter
    fun fromCompletionStyle(style: CompletionStyle?): String? {
        return style?.name
    }

    @TypeConverter
    fun toCompletionStyle(value: String?): CompletionStyle? {
        return value?.let(CompletionStyle::valueOf)
    }

    @TypeConverter
    fun fromStoredTags(tags: List<StoredTag>): String {
        return json.encodeToString(tags)
    }

    @TypeConverter
    fun toStoredTags(value: String): List<StoredTag> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromStoredCompanies(companies: List<StoredCompany>): String {
        return json.encodeToString(companies)
    }

    @TypeConverter
    fun toStoredCompanies(value: String): List<StoredCompany> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromStringList(values: List<String>): String {
        return json.encodeToString(values)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromStoredPlatforms(platforms: List<StoredPlatform>): String {
        return json.encodeToString(platforms)
    }

    @TypeConverter
    fun toStoredPlatforms(value: String): List<StoredPlatform> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromAgeRating(ageRating: StoredAgeRating?): String? {
        return ageRating?.let {
            json.encodeToString(it)
        }
    }

    @TypeConverter
    fun toAgeRating(value: String?): StoredAgeRating? {
        return value?.let {
            json.decodeFromString(it)
        }
    }
}
