package com.example.gamest.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SteamProfileUrlParserTest {

    @Test
    fun parse_vanityProfileUrl_returnsVanityName() {
        val reference = SteamProfileUrlParser.parse(
            "https://steamcommunity.com/id/stepanstepanxyu/"
        )

        assertEquals(
            SteamProfileReferenceType.VANITY_NAME,
            reference.type
        )
        assertEquals("stepanstepanxyu", reference.value)
    }

    @Test
    fun parse_profileUrlWithoutScheme_isSupported() {
        val reference = SteamProfileUrlParser.parse(
            "steamcommunity.com/id/player_name"
        )

        assertEquals(
            SteamProfileReferenceType.VANITY_NAME,
            reference.type
        )
        assertEquals("player_name", reference.value)
    }

    @Test
    fun parse_numericProfileUrl_returnsSteamId() {
        val reference = SteamProfileUrlParser.parse(
            "https://steamcommunity.com/profiles/76561198000000000/"
        )

        assertEquals(
            SteamProfileReferenceType.STEAM_ID,
            reference.type
        )
        assertEquals("76561198000000000", reference.value)
    }

    @Test
    fun parse_nonSteamHost_throws() {
        assertThrows(InvalidSteamProfileUrlException::class.java) {
            SteamProfileUrlParser.parse(
                "https://example.com/id/player"
            )
        }
    }

    @Test
    fun parse_nonProfileSteamUrl_throws() {
        assertThrows(InvalidSteamProfileUrlException::class.java) {
            SteamProfileUrlParser.parse(
                "https://steamcommunity.com/groups/example"
            )
        }
    }

    @Test
    fun parse_malformedNumericSteamId_throws() {
        assertThrows(InvalidSteamProfileUrlException::class.java) {
            SteamProfileUrlParser.parse(
                "https://steamcommunity.com/profiles/12345"
            )
        }
    }
}
