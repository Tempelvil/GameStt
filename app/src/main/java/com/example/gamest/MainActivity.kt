package com.example.gamest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamest.ui.navigation.GameShelfApp
import com.example.gamest.ui.screens.search.SearchScreen
import com.example.gamest.ui.screens.search.SearchViewModel
import com.example.gamest.ui.theme.GameStTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            GameStTheme(darkTheme = isDarkTheme) {
                GameShelfApp(
                    isDarkTheme = isDarkTheme,
                    onThemeClick = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}
