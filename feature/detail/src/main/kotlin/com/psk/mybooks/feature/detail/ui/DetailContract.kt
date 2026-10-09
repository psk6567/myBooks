package com.psk.mybooks.feature.detail.ui

import com.psk.mybooks.core.ui.BookUiModel

/**
 * @param book 보여 줄 도서. 이동 인자로 받는다.
 * @param isFavorite 즐겨찾기 여부. 저장소를 계속 관찰해서 어디서 바꿔도 따라간다.
 */
internal data class DetailUiState(
    val book: BookUiModel,
    val isFavorite: Boolean = false
)

internal sealed interface DetailIntent {
    /** 뒤로가기 버튼을 누름 */
    data object Back : DetailIntent

    /** 즐겨찾기(하트)를 눌러 켜거나 끔 */
    data object ToggleFavorite : DetailIntent
}

/** 한 번만 처리할 일 */
internal sealed interface DetailEffect {
    /** 화면을 닫고 이전 화면으로 돌아감 */
    data object Close : DetailEffect
}
