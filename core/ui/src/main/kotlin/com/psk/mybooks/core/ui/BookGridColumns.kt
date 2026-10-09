package com.psk.mybooks.core.ui

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.dp

/**
 * 도서 격자(검색, 즐겨찾기)의 열 구성. 항목의 최소 폭을 정하고 열 수는 화면 폭에 맞춰 자동으로 정한다.
 * 폰(약 410dp)에서는 3열이고, 태블릿이나 펼친 폴더블, 목록 옆 패널처럼 폭이 달라지면 열 수가 따라 바뀐다.
 */
val BookGridColumns: GridCells = GridCells.Adaptive(minSize = 110.dp)
