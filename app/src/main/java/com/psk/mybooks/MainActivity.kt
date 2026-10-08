package com.psk.mybooks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.feature.main.ui.MainRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyBooksTheme {
                MainRoute()
            }
        }
    }
}
