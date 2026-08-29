package com.example.gamest.data.local

import kotlinx.serialization.Serializable

@Serializable
data class StoredTag(
    val id: Int,
    val name: String,
    val slug: String
)

@Serializable
data class StoredCompany(
    val id: Int,
    val name: String,
    val slug: String
)

@Serializable
data class StoredAgeRating(
    val id: Int,
    val name: String,
    val slug: String
)

@Serializable
data class StoredPlatform(
    val id: Int,
    val name: String,
    val abbreviation: String? = null
)
