package com.example.gamest

import android.app.Application
import com.example.gamest.data.AppContainer
import com.example.gamest.data.DefaultAppContainer

class GameStApplication : Application(){
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}