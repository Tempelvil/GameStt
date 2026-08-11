package com.example.gamest.data.repository

import java.net.URI

enum class SteamProfileReferenceType {
    STEAM_ID,
    VANITY_NAME
}

data class SteamProfileReference(
    val type: SteamProfileReferenceType,
    val value: String
)

object SteamProfileUrlParser {

    private val vanityNamePattern = Regex("^[A-Za-z0-9_-]{2,64}$")
    private val steamIdPattern = Regex("^[0-9]{17}$")

    fun parse(profileUrl: String): SteamProfileReference {
        val trimmedUrl = profileUrl.trim()

        if (trimmedUrl.isBlank()) {
            throw InvalidSteamProfileUrlException(
                "Paste a link to your Steam profile."
            )
        }

        val normalizedUrl = if (
            trimmedUrl.startsWith("https://", ignoreCase = true) ||
            trimmedUrl.startsWith("http://", ignoreCase = true)
        ) {
            trimmedUrl
        } else {
            "https://$trimmedUrl"
        }

        val uri = try {
            URI(normalizedUrl)
        } catch (_: Exception) {
            throw InvalidSteamProfileUrlException(
                "This does not look like a valid Steam profile link."
            )
        }

        val host = uri.host?.lowercase()
        if (host !in supportedHosts) {
            throw InvalidSteamProfileUrlException(
                "Use a steamcommunity.com profile link."
            )
        }

        val pathSegments = uri.path
            .split('/')
            .filter(String::isNotBlank)

        if (pathSegments.size != 2) {
            throw InvalidSteamProfileUrlException(
                "Use a link like steamcommunity.com/id/name or /profiles/SteamID."
            )
        }

        val profileType = pathSegments[0].lowercase()
        val profileValue = pathSegments[1]

        return when (profileType) {
            "id" -> {
                if (!vanityNamePattern.matches(profileValue)) {
                    throw InvalidSteamProfileUrlException(
                        "The custom Steam profile name is invalid."
                    )
                }

                SteamProfileReference(
                    type = SteamProfileReferenceType.VANITY_NAME,
                    value = profileValue
                )
            }

            "profiles" -> {
                if (!steamIdPattern.matches(profileValue)) {
                    throw InvalidSteamProfileUrlException(
                        "The Steam profile ID must contain 17 digits."
                    )
                }

                SteamProfileReference(
                    type = SteamProfileReferenceType.STEAM_ID,
                    value = profileValue
                )
            }

            else -> throw InvalidSteamProfileUrlException(
                "This link does not point to an individual Steam profile."
            )
        }
    }

    private val supportedHosts = setOf(
        "steamcommunity.com",
        "www.steamcommunity.com"
    )
}

class InvalidSteamProfileUrlException(
    message: String
) : IllegalArgumentException(message)
