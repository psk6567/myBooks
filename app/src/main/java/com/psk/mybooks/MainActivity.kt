package com.psk.mybooks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.dev.DevEntry
import com.psk.mybooks.feature.main.ui.MainRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyBooksTheme {
                // debug 빌드에서만 제목을 여러 번 누르면 개발자 모드가 열린다. release 에서는 null 이다.
                MainRoute(onTitleClick = DevEntry.titleClick(this@MainActivity))
            }
        }
    }
}
