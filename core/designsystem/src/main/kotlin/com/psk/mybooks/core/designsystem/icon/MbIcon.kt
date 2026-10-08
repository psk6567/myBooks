package com.psk.mybooks.core.designsystem.icon

import androidx.annotation.DrawableRes
import com.psk.mybooks.core.designsystem.R

/**
 * 디자인 시스템이 제공하는 아이콘. 컴포넌트는 아이콘을 이 타입으로만 받는다.
 * 리소스 id 는 internal 이라 feature 에서 임의의 drawable 을 넘길 수 없다.
 * 새 아이콘이 필요하면 res/drawable 에 추가하고 여기에 항목을 추가한다.
 */
enum class MbIcon(@DrawableRes internal val resId: Int) {
    ArrowBack(R.drawable.ic_arrow_back),
    Close(R.drawable.ic_close),
    Favorite(R.drawable.ic_favorite),
    Search(R.drawable.ic_search)
}
