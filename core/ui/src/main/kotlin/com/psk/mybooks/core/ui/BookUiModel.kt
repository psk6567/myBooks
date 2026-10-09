package com.psk.mybooks.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.isOnSale
import java.text.NumberFormat
import kotlinx.serialization.Serializable

/**
 * 도서 목록 항목(검색, 즐겨찾기)과 상세 화면이 그리는 값. 화면은 도메인 모델([Book])이 아니라 이것만 안다.
 * 원시 타입(문자열, 숫자)만 가져서 도메인이 바뀌어도 화면이 영향을 받지 않고, 리컴포지션 판단도 안정적이다.
 * 표시용으로 가공한 값(저자 합치기, 할인 여부)은 [Book.toUiModel] 한 곳에서 만든다.
 * 상세 화면으로 이동할 때 이동 인자(JSON)로 넘기므로 [Serializable] 이다.
 *
 * 하트를 눌러 저장하려면 도서 전체가 필요하지만 여기에는 표시에 필요한 값만 있다.
 * 원본 [Book] 은 ViewModel 이 들고 있고, 화면은 [id] 만 넘긴다.
 *
 * @param id 도서를 구분하는 값. 하트를 눌러 저장할 때 원본 도서를 찾는 키다.
 * @param status 판매 상태(정상판매, 품절 등). 값으로 분기하지 않고 화면에 보여 주기만 한다.
 * @param title 도서 제목
 * @param contents 도서 소개. 목록용으로 잘려서 온다.
 * @param url 도서 상세 URL
 * @param isbn ISBN10 과 ISBN13 중 하나 이상. 둘 다 있으면 공백으로 구분한다.
 * @param publishedAt 출판일(`2013-12-24`). 알 수 없으면 빈 문자열.
 * @param publisher 출판사
 * @param authors 화면에 보일 저자. 여러 명이면 쉼표로 이어 붙인 한 줄이다.
 * @param translators 화면에 보일 번역자. 여러 명이면 쉼표로 이어 붙인 한 줄이고 없으면 빈 문자열이다.
 * @param price 정가(원)
 * @param salePrice 정가와 다른 판매가(원). 같거나 알 수 없으면 null 이라 정가만 보인다.
 * @param thumbnailUrl 표지 이미지 URL. 없으면 빈 문자열.
 */
@Serializable
data class BookUiModel(
    val id: String,
    val status: String,
    val title: String,
    val contents: String,
    val url: String,
    val isbn: String,
    val publishedAt: String,
    val publisher: String,
    val authors: String,
    val translators: String,
    val price: Int,
    val salePrice: Int?,
    val thumbnailUrl: String
)

/** `yyyy-MM-dd` 의 길이 */
private const val PUBLISHED_DATE_LENGTH: Int = 10

/** 도메인 모델을 화면용으로 바꾼다. */
fun Book.toUiModel(): BookUiModel = BookUiModel(
    id = id,
    status = status,
    title = title,
    contents = contents,
    url = url,
    isbn = isbn.trim(),
    // API 가 주는 ISO 8601(2013-12-24T00:00:00.000+09:00)에서 날짜만 보인다
    publishedAt = publishedAt.take(PUBLISHED_DATE_LENGTH),
    publisher = publisher,
    authors = authors.joinToString(", "),
    translators = translators.joinToString(", "),
    price = price,
    salePrice = if (isOnSale) salePrice else null,
    thumbnailUrl = thumbnailUrl
)

/**
 * 화면용 값을 도메인 모델로 되돌린다. 상세 화면이 즐겨찾기에 담을 때 도서 전체가 필요해서 쓴다.
 *
 * 화면에 보이지 않는 부분은 되살릴 수 없다. 출판일은 날짜만 남고(시각은 없음), 저자·번역자 이름 안에 `, ` 가 있으면
 * 여러 명으로 나뉜다. 그래도 이 값을 다시 [toUiModel] 로 바꾸면 같은 값이 되므로 화면에 보이는 것은 달라지지 않는다.
 * 판매가를 모르거나 정가와 같으면 [Book.salePrice] 는 `-1`(알 수 없음)이다.
 */
fun BookUiModel.toBook(): Book = Book(
    id = id,
    title = title,
    contents = contents,
    url = url,
    isbn = isbn,
    publishedAt = publishedAt,
    authors = authors.splitNames(),
    publisher = publisher,
    translators = translators.splitNames(),
    price = price,
    salePrice = salePrice ?: UNKNOWN_SALE_PRICE,
    thumbnailUrl = thumbnailUrl,
    status = status
)

/** [Book.salePrice] 가 알 수 없을 때 쓰는 값 */
private const val UNKNOWN_SALE_PRICE: Int = -1

/** 쉼표로 이어 붙인 이름을 다시 나눈다. 비어 있으면 빈 목록이다. */
private fun String.splitNames(): List<String> = if (isBlank()) emptyList() else split(", ")

/** 정가를 화면에 보일 글자로 만든다. (예: 15,000원) */
@Composable
fun BookUiModel.priceText(): String = stringResource(R.string.core_ui_price, formatPrice(price))

/** 판매가를 화면에 보일 글자로 만든다. 정가와 다르지 않으면 null 이다. */
@Composable
fun BookUiModel.salePriceText(): String? = salePrice?.let { stringResource(R.string.core_ui_price, formatPrice(it)) }

/** 천 단위 구분 (예: 15000 → 15,000) */
private fun formatPrice(price: Int): String = NumberFormat.getNumberInstance().format(price)
