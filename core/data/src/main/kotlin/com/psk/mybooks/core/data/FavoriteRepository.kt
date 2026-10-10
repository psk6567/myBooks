package com.psk.mybooks.core.data

import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import kotlinx.coroutines.flow.Flow

/**
 * 즐겨찾기한 도서의 저장소. 기기에 저장되어 앱을 다시 켜도 남는다.
 * 읽기는 모두 Flow 라서, 어디에서 추가·삭제하든 구독 중인 화면(검색, 즐겨찾기, 상세)이 알아서 갱신된다.
 */
interface FavoriteRepository {

    /**
     * 즐겨찾기 목록에서 [query] 와 [priceFilter] 에 맞는 도서만 골라 [sort] 순서로 준다.
     * 금액은 판매가 기준이다. 기준이 같으면 최근에 담은 것이 먼저다. 비어 있으면 빈 목록이다.
     *
     * @param query 검색어. 제목, 저자, 출판사 중 하나에 들어 있으면 맞는 것으로 본다. 대소문자는 구분하지 않고 앞뒤 공백은 무시하며,
     * 비어 있으면 거르지 않는다. (서버가 아니라 기기에 저장된 목록 안에서 찾는다)
     */
    fun observeFavorites(
        sort: FavoriteSortType,
        priceFilter: PriceFilter = PriceFilter(),
        query: String = ""
    ): Flow<List<Book>>

    /** 즐겨찾기한 도서의 id 모음. 검색 결과나 상세 화면이 하트를 그릴 때 쓴다. */
    fun observeFavoriteIds(): Flow<Set<String>>

    /** [book] 을 즐겨찾기에 담는다. 이미 있으면 최신 값으로 바꾸고 담은 시각도 새로 기록한다. */
    suspend fun add(book: Book)

    /** [bookId] 를 즐겨찾기에서 뺀다. 없는 id 면 아무 일도 없다. */
    suspend fun remove(bookId: String)
}
