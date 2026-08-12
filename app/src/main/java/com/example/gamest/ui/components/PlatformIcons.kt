package com.example.gamest.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gamest.R

@Composable
fun PlatformIcons(
    platforms: List<String>,
    modifier: Modifier = Modifier,
    iconSize: Dp = 14.dp,
    maxIcons: Int = 3,
    showLeadingSeparator: Boolean = false
) {
    val icons = platforms
        .mapNotNull { platform ->
            platform.toPlatformIcon()?.let { icon -> platform to icon }
        }
        .distinctBy { (_, icon) -> icon }
        .take(maxIcons)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLeadingSeparator && icons.isNotEmpty()) {
            Text(
                text = "•",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        icons.forEach { (platform, icon) ->
            Icon(
                painter = painterResource(icon),
                contentDescription = platform,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@DrawableRes
private fun String.toPlatformIcon(): Int? {
    val normalizedName = lowercase()
    return when {
        normalizedName == "pc" || "windows" in normalizedName ->
            R.drawable.logo_windows_svgrepo_com
        "playstation" in normalizedName ->
            R.drawable.play_station_logo_svgrepo_com
        "xbox" in normalizedName -> R.drawable.xbox_svgrepo_com
        "nintendo" in normalizedName || "switch" in normalizedName ->
            R.drawable.nintendo_switch_svgrepo_com
        "android" in normalizedName -> R.drawable.android_svgrepo_com
        normalizedName == "ios" || "macos" in normalizedName ->
            R.drawable.ios_svgrepo_com
        "linux" in normalizedName -> R.drawable.linux_svgrepo_com
        else -> null
    }
}
