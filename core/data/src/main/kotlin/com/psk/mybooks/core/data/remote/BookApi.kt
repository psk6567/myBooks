package com.psk.mybooks.core.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** 카카오 도서 검색 API. 인증 헤더는 `NetworkModule` 의 OkHttp 인터셉터가 붙인다. */
internal interface BookApi {

    /**
     * @param query 검색어 (필수)
     * @param sort 정렬 방식. 기본값 [SearchSort.Accuracy]
     * @param page 결과 페이지 번호, 1~50. 기본값 1
     * @param size 한 페이지에 보여질 문서 수, 1~50. 기본값 10
     * @param target 검색 필드 제한. null 이면 보내지 않아 전체에서 찾는다.
     */
    @GET("v3/search/book")
    suspend fun searchBooks(
        @Query("query") query: String,
        @Query("sort") sort: SearchSort = SearchSort.Accuracy,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10,
        @Query("target") target: SearchTarget? = null
    ): BookSearchResponse
}

/** 결과 문서 정렬 방식. Retrofit 이 `toString()` 을 쿼리 값으로 쓰므로 API 값을 돌려준다. */
internal enum class SearchSort(private val value: String) {
    /** 정확도순 */
    Accuracy("accuracy"),

    /** 발간일순 */
    Latest("latest");

    override fun toString(): String = value
}

/** 검색 필드 제한 */
internal enum class SearchTarget(private val value: String) {
    Title("title"),
    Isbn("isbn"),
    Publisher("publisher"),
    Person("person");

    override fun toString(): String = value
}
