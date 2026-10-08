package com.psk.mybooks.feature.search.ui

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState

/**
 * 검색을 마쳤는데 결과가 0건인지.
 *
 * 처음 불러오기(refresh)가 끝났고 더 불러올 페이지가 없다(append 가 끝에 도달)는 점으로 판단한다.
 * (Paging 은 `refresh` 의 endOfPaginationReached 를 항상 false 로 두고, 끝에 도달했는지는 append 로 알린다)
 * 검색어가 비어 불러오지 않은 빈 목록(`PagingData.empty()`)은 로드 상태가 없어 화면이 "불러오는 중"으로 두므로 해당하지 않는다.
 * 그래서 글자를 입력한 직후 "결과 없음"이 잠깐 보이지 않는다.
 */
internal fun CombinedLoadStates.isNoResult(itemCount: Int): Boolean =
    itemCount == 0 && refresh is LoadState.NotLoading && append.endOfPaginationReached
