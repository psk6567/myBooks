package com.psk.mybooks.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * [key] 가 바뀌면 [scrollToTop] 을 부른다. 정렬이나 필터를 바꾸면 목록의 내용이 달라지는데 스크롤 위치만 그대로 남아
 * 어디를 보고 있는지 헷갈리므로, 맨 위로 올려 새 목록의 처음부터 보이게 한다.
 *
 * 처음 그릴 때와 화면 회전 뒤 다시 그릴 때는 부르지 않는다. (그러면 회전할 때마다 보던 위치를 잃는다)
 *
 * @param key 이 값이 바뀌었을 때만 맨 위로 올린다. 정렬 기준, 필터 같은 "목록을 다시 만드는 조건"을 묶어서 넘긴다.
 */
@Composable
fun ScrollToTopOnChange(key: Any?, scrollToTop: suspend () -> Unit) {
    val initial: Any? = remember { key }
    LaunchedEffect(key) {
        if (key != initial) scrollToTop()
    }
}
