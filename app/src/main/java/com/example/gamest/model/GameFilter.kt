package com.example.gamest.model

sealed class GameFilter(val title: String) {
    data object TopRated : GameFilter("Top Rated")
    data object Rpg : GameFilter("RPG")
    data object Action : GameFilter("Action")
    data object Pc : GameFilter("PC")
}