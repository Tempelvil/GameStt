package com.example.gamest.model

sealed class GameFilter(val title: String) {
    open val sort: String = "users"
    open val genreId: Int? = null
    open val platformId: Int? = null
    open val developerId: Int? = null
    open val publisherId: Int? = null
    open val ageRatingCategoryId: Int? = null
    open val minimumRatings: Int? = null
    open val maxResults: Int? = null
    open val topOnly: Boolean? = null

    data object TopRated : GameFilter("Top Rated") {
        override val minimumRatings: Int = 500
        override val maxResults: Int = 100
        override val topOnly: Boolean = true
    }
    data object Rpg : GameFilter("RPG") {
        override val genreId: Int = 12
    }
    data object Action : GameFilter("Action") {
        override val genreId: Int = 4
    }
    data object Pc : GameFilter("PC") {
        override val platformId: Int = 6
    }

    data class Genres(
        val id: Int,
        val name: String,
        val slug: String
    ): GameFilter(name) {
        override val genreId: Int = id
    }

    data class Developer(
        val id: Int,
        val name: String
    ): GameFilter(name) {
        override val developerId: Int = id
    }

    data class Publisher(
        val id: Int,
        val name: String
    ): GameFilter(name) {
        override val publisherId: Int = id
    }

    data class AgeRating(
        val id: Int,
        val name: String
    ): GameFilter(name) {
        override val ageRatingCategoryId: Int = id
    }
}
