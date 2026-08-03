package com.example.gamest.model

sealed class GameFilter(val title: String) {
    data object TopRated : GameFilter("Top Rated")
    data object Rpg : GameFilter("RPG")
    data object Action : GameFilter("Action")
    data object Pc : GameFilter("PC")

    data class Genres(
        val id: Int,
        val name: String,
        val slug: String
    ): GameFilter(name)

    data class Developer(
        val id: Int,
        val name: String
    ): GameFilter(name)

    data class Publisher(
        val id: Int,
        val name: String
    ): GameFilter(name)

    data class AgeRating(
        val id: Int,
        val name: String
    ): GameFilter(name)
}