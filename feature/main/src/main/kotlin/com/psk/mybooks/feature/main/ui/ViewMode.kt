package com.psk.mybooks.feature.main.ui

/**
 * 도서 목록을 보여 주는 방식. 보기 방식을 정하는 건 main 이고, 각 목록 화면에는 격자 여부(Boolean)로 넘긴다.
 * 선언 순서가 전환 토글에 보이는 순서다.
 */
internal enum class ViewMode { List, Grid }
