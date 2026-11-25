package com.bloom.habittracker

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bloom.habittracker.data.HabitStore
import com.bloom.habittracker.ui.BloomApp
import com.bloom.habittracker.ui.BloomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        val store = HabitStore(this)
        setContent {
            BloomTheme { BloomApp(store) }
        }
    }
}
