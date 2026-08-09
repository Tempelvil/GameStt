package com.example.gamest.data.offline

import com.example.gamest.data.network.RawgDeveloperDto
import com.example.gamest.data.network.RawgEsrbRatingDto
import com.example.gamest.data.network.RawgGameDetailsDto
import com.example.gamest.data.network.RawgGameDto
import com.example.gamest.data.network.RawgGenreDto
import com.example.gamest.data.network.RawgPlatformDto
import com.example.gamest.data.network.RawgPlatformWrapperDto
import com.example.gamest.data.network.RawgPublisherDto

data class OfflineGame(
    val details: RawgGameDetailsDto,
    val screenshots: List<String>
) {

    fun toGameDto(): RawgGameDto {
        return RawgGameDto(
            id = details.id,
            name = details.name,
            backgroundImage = details.backgroundImage,
            rating = details.rating,
            genres = details.genres,
            platforms = details.platforms,
            metacritic = details.metacritic
        )
    }
}

object OfflineGamesData {
    val action = RawgGenreDto(4, "Action", "action")

    val rpg = RawgGenreDto(
        5,
        "RPG",
        "role-playing-games-rpg"
    )

    val shooter = RawgGenreDto(
        2,
        "Shooter",
        "shooter"
    )

    val adventure = RawgGenreDto(
        3,
        "Adventure",
        "adventure"
    )

    val strategy = RawgGenreDto(
        10,
        "Strategy",
        "strategy"
    )

    val indie = RawgGenreDto(
        51,
        "Indie",
        "indie"
    )

    val puzzle = RawgGenreDto(
        7,
        "Puzzle",
        "puzzle"
    )

    val simulation = RawgGenreDto(
        14,
        "Simulation",
        "simulation"
    )

    // Platforms

    private val pc = platform(
        id = 4,
        name = "PC",
        slug = "pc"
    )

    private val ps5 = platform(
        id = 187,
        name = "PlayStation 5",
        slug = "playstation5"
    )

    private val ps4 = platform(
        id = 18,
        name = "PlayStation 4",
        slug = "playstation4"
    )

    private val xbox = platform(
        id = 186,
        name = "Xbox Series S/X",
        slug = "xbox-series-x"
    )

    private val switch = platform(
        id = 7,
        name = "Nintendo Switch",
        slug = "nintendo-switch"
    )

    private val android = platform(
        id = 21,
        name = "Android",
        slug = "android"
    )

    private val ios = platform(
        id = 3,
        name = "iOS",
        slug = "ios"
    )


    private val everyone = RawgEsrbRatingDto(
        id = 1,
        name = "Everyone",
        slug = "everyone"
    )

    private val teen = RawgEsrbRatingDto(
        id = 3,
        name = "Teen",
        slug = "teen"
    )

    private val mature = RawgEsrbRatingDto(
        id = 4,
        name = "Mature",
        slug = "mature"
    )

    val games: List<OfflineGame> = listOf(

        game(
            id = 1,
            name = "The Witcher 3: Wild Hunt",
            description = "A story-driven open-world RPG about Geralt of Rivia searching for Ciri while war and supernatural threats reshape the Northern Kingdoms.",
            released = "2015-05-19",
            rating = 4.7,
            metacritic = 93,
            genres = listOf(rpg, adventure),
            platforms = listOf(pc, ps4, ps5, xbox, switch),
            developer = developer(1, "CD Projekt Red"),
            publisher = publisher(101, "CD Projekt"),
            playtime = 52,
            esrb = mature
        ),

        game(
            id = 2,
            name = "Cyberpunk 2077",
            description = "An open-world action RPG set in Night City where a mercenary named V becomes entangled with a dangerous digital construct.",
            released = "2020-12-10",
            rating = 4.4,
            metacritic = 86,
            genres = listOf(rpg, action),
            platforms = listOf(pc, ps5, xbox),
            developer = developer(1, "CD Projekt Red"),
            publisher = publisher(101, "CD Projekt"),
            playtime = 27,
            esrb = mature
        ),

        game(
            id = 3,
            name = "Baldur's Gate 3",
            description = "A party-based role-playing game set in the Forgotten Realms with turn-based combat and highly reactive storytelling.",
            released = "2023-08-03",
            rating = 4.8,
            metacritic = 96,
            genres = listOf(rpg, strategy),
            platforms = listOf(pc, ps5, xbox),
            developer = developer(2, "Larian Studios"),
            publisher = publisher(102, "Larian Studios"),
            playtime = 65,
            esrb = mature
        ),

        game(
            id = 4,
            name = "Elden Ring",
            description = "An open-world action RPG created by FromSoftware in which the player explores the Lands Between and faces powerful enemies.",
            released = "2022-02-25",
            rating = 4.7,
            metacritic = 96,
            genres = listOf(rpg, action),
            platforms = listOf(pc, ps4, ps5, xbox),
            developer = developer(3, "FromSoftware"),
            publisher = publisher(103, "Bandai Namco"),
            playtime = 58,
            esrb = mature
        ),

        game(
            id = 5,
            name = "Red Dead Redemption 2",
            description = "An open-world western following outlaw Arthur Morgan and the Van der Linde gang during the decline of the American frontier.",
            released = "2018-10-26",
            rating = 4.8,
            metacritic = 97,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps4, xbox),
            developer = developer(4, "Rockstar Games"),
            publisher = publisher(104, "Rockstar Games"),
            playtime = 50,
            esrb = mature
        ),

        game(
            id = 6,
            name = "Grand Theft Auto V",
            description = "An open-world crime action game centered on three protagonists whose stories intersect across Los Santos.",
            released = "2013-09-17",
            rating = 4.5,
            metacritic = 97,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps4, ps5, xbox),
            developer = developer(4, "Rockstar Games"),
            publisher = publisher(104, "Rockstar Games"),
            playtime = 32,
            esrb = mature
        ),

        game(
            id = 7,
            name = "Hades",
            description = "A fast-paced roguelike in which Zagreus repeatedly attempts to escape the Underworld.",
            released = "2020-09-17",
            rating = 4.6,
            metacritic = 93,
            genres = listOf(action, indie),
            platforms = listOf(pc, ps4, ps5, xbox, switch),
            developer = developer(5, "Supergiant Games"),
            publisher = publisher(105, "Supergiant Games"),
            playtime = 22,
            esrb = teen
        ),

        game(
            id = 8,
            name = "Hades II",
            description = "A roguelike sequel following Melinoë as she battles through a mythological underworld.",
            released = "2025-09-25",
            rating = 4.6,
            metacritic = 91,
            genres = listOf(action, rpg, indie),
            platforms = listOf(pc, switch),
            developer = developer(5, "Supergiant Games"),
            publisher = publisher(105, "Supergiant Games"),
            playtime = 24,
            esrb = teen
        ),

        game(
            id = 9,
            name = "Hollow Knight",
            description = "A hand-drawn action adventure through the ruined underground kingdom of Hallownest.",
            released = "2017-02-24",
            rating = 4.6,
            metacritic = 90,
            genres = listOf(action, adventure, indie),
            platforms = listOf(pc, ps4, xbox, switch),
            developer = developer(6, "Team Cherry"),
            publisher = publisher(106, "Team Cherry"),
            playtime = 27,
            esrb = teen
        ),

        game(
            id = 10,
            name = "DOOM Eternal",
            description = "A high-speed first-person shooter focused on aggressive combat against demonic armies.",
            released = "2020-03-20",
            rating = 4.4,
            metacritic = 88,
            genres = listOf(action, shooter),
            platforms = listOf(pc, ps4, ps5, xbox, switch),
            developer = developer(7, "id Software"),
            publisher = publisher(107, "Bethesda Softworks"),
            playtime = 14,
            esrb = mature
        ),

        game(
            id = 11,
            name = "God of War",
            description = "Kratos and his son Atreus journey through the realms of Norse mythology.",
            released = "2018-04-20",
            rating = 4.7,
            metacritic = 94,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps4),
            developer = developer(8, "Santa Monica Studio"),
            publisher = publisher(108, "Sony Interactive Entertainment"),
            playtime = 21,
            esrb = mature
        ),

        game(
            id = 12,
            name = "God of War Ragnarök",
            description = "Kratos and Atreus face the coming of Ragnarök while traveling through the Nine Realms.",
            released = "2022-11-09",
            rating = 4.7,
            metacritic = 94,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps4, ps5),
            developer = developer(8, "Santa Monica Studio"),
            publisher = publisher(108, "Sony Interactive Entertainment"),
            playtime = 26,
            esrb = mature
        ),

        game(
            id = 13,
            name = "The Last of Us Part I",
            description = "Joel escorts Ellie across a devastated United States in a story-focused action adventure.",
            released = "2022-09-02",
            rating = 4.5,
            metacritic = 88,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps5),
            developer = developer(9, "Naughty Dog"),
            publisher = publisher(108, "Sony Interactive Entertainment"),
            playtime = 15,
            esrb = mature
        ),

        game(
            id = 14,
            name = "Death Stranding",
            description = "Sam Porter Bridges crosses a fractured America to reconnect isolated communities.",
            released = "2019-11-08",
            rating = 4.2,
            metacritic = 86,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps4, ps5),
            developer = developer(10, "Kojima Productions"),
            publisher = publisher(109, "505 Games"),
            playtime = 40,
            esrb = mature
        ),

        game(
            id = 15,
            name = "Control",
            description = "A supernatural action game set inside the shifting headquarters of the Federal Bureau of Control.",
            released = "2019-08-27",
            rating = 4.2,
            metacritic = 85,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps4, ps5, xbox),
            developer = developer(11, "Remedy Entertainment"),
            publisher = publisher(110, "505 Games"),
            playtime = 13,
            esrb = mature
        ),

        game(
            id = 16,
            name = "Alan Wake 2",
            description = "A survival horror story following writer Alan Wake and FBI agent Saga Anderson.",
            released = "2023-10-27",
            rating = 4.5,
            metacritic = 89,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps5, xbox),
            developer = developer(11, "Remedy Entertainment"),
            publisher = publisher(111, "Epic Games"),
            playtime = 18,
            esrb = mature
        ),

        game(
            id = 17,
            name = "Resident Evil 4",
            description = "Leon Kennedy travels to rural Europe to rescue the president's daughter from a mysterious cult.",
            released = "2023-03-24",
            rating = 4.6,
            metacritic = 93,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps4, ps5, xbox),
            developer = developer(12, "Capcom"),
            publisher = publisher(112, "Capcom"),
            playtime = 16,
            esrb = mature
        ),

        game(
            id = 18,
            name = "Resident Evil Village",
            description = "Ethan Winters searches for his daughter in a mysterious European village.",
            released = "2021-05-07",
            rating = 4.4,
            metacritic = 84,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps4, ps5, xbox),
            developer = developer(12, "Capcom"),
            publisher = publisher(112, "Capcom"),
            playtime = 10,
            esrb = mature
        ),

        game(
            id = 19,
            name = "Dark Souls III",
            description = "A dark fantasy action RPG built around challenging combat and exploration.",
            released = "2016-04-12",
            rating = 4.6,
            metacritic = 89,
            genres = listOf(rpg, action),
            platforms = listOf(pc, ps4, xbox),
            developer = developer(3, "FromSoftware"),
            publisher = publisher(103, "Bandai Namco"),
            playtime = 32,
            esrb = mature
        ),

        game(
            id = 20,
            name = "Sekiro: Shadows Die Twice",
            description = "A shinobi fights to protect his young lord in a brutal reinterpretation of Sengoku-era Japan.",
            released = "2019-03-22",
            rating = 4.6,
            metacritic = 90,
            genres = listOf(action, adventure),
            platforms = listOf(pc, ps4, xbox),
            developer = developer(3, "FromSoftware"),
            publisher = publisher(113, "Activision"),
            playtime = 30,
            esrb = mature
        ),

        game(
            id = 21,
            name = "Disco Elysium",
            description = "A dialogue-heavy detective RPG in which skills and personality traits shape every investigation.",
            released = "2019-10-15",
            rating = 4.5,
            metacritic = 91,
            genres = listOf(rpg, indie),
            platforms = listOf(pc, ps4, ps5, xbox, switch),
            developer = developer(13, "ZA/UM"),
            publisher = publisher(114, "ZA/UM"),
            playtime = 23,
            esrb = mature
        ),

        game(
            id = 22,
            name = "Stardew Valley",
            description = "A farming and life simulation about rebuilding an inherited farm and becoming part of a small community.",
            released = "2016-02-26",
            rating = 4.6,
            metacritic = 89,
            genres = listOf(rpg, simulation, indie),
            platforms = listOf(pc, ps4, xbox, switch, android, ios),
            developer = developer(14, "ConcernedApe"),
            publisher = publisher(115, "ConcernedApe"),
            playtime = 53,
            esrb = everyone
        ),

        game(
            id = 23,
            name = "Terraria",
            description = "A sandbox adventure built around exploration, crafting, construction and combat.",
            released = "2011-05-16",
            rating = 4.5,
            metacritic = 83,
            genres = listOf(action, adventure, indie),
            platforms = listOf(pc, ps4, xbox, switch, android, ios),
            developer = developer(15, "Re-Logic"),
            publisher = publisher(116, "Re-Logic"),
            playtime = 45,
            esrb = teen
        ),

        game(
            id = 24,
            name = "Minecraft",
            description = "A sandbox game about exploration, survival and building in procedurally generated worlds.",
            released = "2011-11-18",
            rating = 4.5,
            metacritic = 93,
            genres = listOf(adventure),
            platforms = listOf(pc, ps4, ps5, xbox, switch, android, ios),
            developer = developer(16, "Mojang Studios"),
            publisher = publisher(117, "Xbox Game Studios"),
            playtime = 120,
            esrb = everyone
        ),

        game(
            id = 25,
            name = "Sid Meier's Civilization VI",
            description = "A turn-based strategy game about building and expanding a civilization across human history.",
            released = "2016-10-21",
            rating = 4.3,
            metacritic = 88,
            genres = listOf(strategy, simulation),
            platforms = listOf(pc, ps4, xbox, switch, android, ios),
            developer = developer(17, "Firaxis Games"),
            publisher = publisher(118, "2K"),
            playtime = 80,
            esrb = everyone
        ),

        game(
            id = 26,
            name = "XCOM 2",
            description = "A tactical strategy game in which a resistance force fights an alien occupation of Earth.",
            released = "2016-02-05",
            rating = 4.3,
            metacritic = 88,
            genres = listOf(strategy),
            platforms = listOf(pc, ps4, xbox, switch),
            developer = developer(17, "Firaxis Games"),
            publisher = publisher(118, "2K"),
            playtime = 32,
            esrb = teen
        ),

        game(
            id = 27,
            name = "Portal 2",
            description = "A first-person puzzle game centered on portals, physics and the laboratories of Aperture Science.",
            released = "2011-04-19",
            rating = 4.8,
            metacritic = 95,
            genres = listOf(puzzle, adventure),
            platforms = listOf(pc, xbox, switch),
            developer = developer(18, "Valve"),
            publisher = publisher(119, "Valve"),
            playtime = 9,
            esrb = everyone
        ),

        game(
            id = 28,
            name = "Half-Life 2",
            description = "Gordon Freeman joins a resistance movement against an alien occupation of Earth.",
            released = "2004-11-16",
            rating = 4.7,
            metacritic = 96,
            genres = listOf(shooter, action),
            platforms = listOf(pc, xbox),
            developer = developer(18, "Valve"),
            publisher = publisher(119, "Valve"),
            playtime = 13,
            esrb = mature
        ),

        game(
            id = 29,
            name = "Mass Effect Legendary Edition",
            description = "A remastered trilogy following Commander Shepard's fight against a galaxy-wide threat.",
            released = "2021-05-14",
            rating = 4.7,
            metacritic = 86,
            genres = listOf(rpg, action),
            platforms = listOf(pc, ps4, xbox),
            developer = developer(19, "BioWare"),
            publisher = publisher(120, "Electronic Arts"),
            playtime = 100,
            esrb = mature
        ),

        game(
            id = 30,
            name = "Divinity: Original Sin 2",
            description = "A tactical role-playing game offering party-based combat and extensive freedom of choice.",
            released = "2017-09-14",
            rating = 4.7,
            metacritic = 93,
            genres = listOf(rpg, strategy),
            platforms = listOf(pc, ps4, xbox, switch),
            developer = developer(2, "Larian Studios"),
            publisher = publisher(102, "Larian Studios"),
            playtime = 60,
            esrb = mature
        )
    )

    val genres: List<RawgGenreDto> = listOf(
        action,
        rpg,
        shooter,
        adventure,
        strategy,
        indie,
        puzzle,
        simulation
    )

    private fun game(
        id: Int,
        name: String,
        description: String,
        released: String,
        rating: Double,
        metacritic: Int?,
        genres: List<RawgGenreDto>,
        platforms: List<RawgPlatformWrapperDto>,
        developer: RawgDeveloperDto,
        publisher: RawgPublisherDto,
        playtime: Int,
        esrb: RawgEsrbRatingDto?
    ): OfflineGame {

        val image =
            "https://picsum.photos/seed/gamest-cover-$id/900/600"

        return OfflineGame(
            details = RawgGameDetailsDto(
                id = id,
                name = name,
                backgroundImage = image,
                descriptionRaw = description,
                released = released,
                rating = rating,
                metacritic = metacritic,
                genres = genres,
                platforms = platforms,
                developers = listOf(developer),
                publishers = listOf(publisher),
                playtime = playtime,
                esrbRating = esrb
            ),

            screenshots = List(4) { index ->
                "https://picsum.photos/seed/gamest-$id-$index/1200/700"
            }
        )
    }

    private fun platform(
        id: Int,
        name: String,
        slug: String
    ): RawgPlatformWrapperDto {
        return RawgPlatformWrapperDto(
            platform = RawgPlatformDto(
                id = id,
                name = name,
                slug = slug
            )
        )
    }

    private fun company(
        id: Int,
        name: String
    ): RawgDeveloperDto {
        return RawgDeveloperDto(
            id = id,
            name = name,
            slug = name
                .lowercase()
                .replace(" ", "-")
                .replace("/", "")
        )
    }

    private fun companyPublisher(
        id: Int,
        name: String
    ): RawgPublisherDto {
        return RawgPublisherDto(
            id = id,
            name = name,
            slug = name
                .lowercase()
                .replace(" ", "-")
                .replace("/", "")
        )
    }

}
private fun developer(
    id: Int,
    name: String
): RawgDeveloperDto {
    return RawgDeveloperDto(
        id = id,
        name = name,
        slug = name.toSlug()
    )
}

private fun publisher(
    id: Int,
    name: String
): RawgPublisherDto {
    return RawgPublisherDto(
        id = id,
        name = name,
        slug = name.toSlug()
    )
}

private fun String.toSlug(): String {
    return lowercase()
        .replace(" ", "-")
        .replace("/", "")
}