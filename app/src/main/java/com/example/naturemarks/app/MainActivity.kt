package com.example.naturemarks.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.naturemarks.app.navigation.NavigationGraph
import com.example.naturemarks.ui.theme.NatureMarksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NatureMarksTheme {
                NavigationGraph()
            }
        }
    }
}