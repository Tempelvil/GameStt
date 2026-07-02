package com.example.gamest.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.gamest.ui.theme.GameStTheme

@Composable
fun GameBottomBar(
    selectedRoute: String,
    onItemClick:(String)-> Unit
){

    NavigationBar {
        NavigationBarItem(
            selected = selectedRoute == "search",
            onClick = {onItemClick("search")},
            icon = {Icon(Icons.Default.Search, contentDescription = null)},
            label = { Text("Search") }
        )
        NavigationBarItem(
            selected = selectedRoute == "collection",
            onClick = { onItemClick("collection") },
            icon = { Icon(Icons.Default.CollectionsBookmark, contentDescription = null) },
            label = { Text("Collection") }
        )
        NavigationBarItem(
            selected = selectedRoute == "statistics",
            onClick = { onItemClick("statistics") },
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Statistics") }
        )
    }
}
@Preview(showBackground = true)
@Composable
fun GameBottomBarPreview() {
    GameStTheme {
        GameBottomBar(
            selectedRoute = "search",
            onItemClick = {}
        )
    }
}
@Preview(showBackground = true)
@Composable
fun GameBottomBarDarkPreview() {
    GameStTheme(darkTheme = true) {
        GameBottomBar(
            selectedRoute = "search",
            onItemClick = {}
        )
    }
}