package com.psk.mybooks.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.isOnSale
import java.text.NumberFormat

/**
 * 도서 목록 항목(검색, 즐겨찾기)이 그리는 값. 화면은 도메인 모델([Book])이 아니라 이것만 안다.
 * 원시 타입(문자열, 숫자)만 가져서 도메인이 바뀌어도 화면이 영향을 받지 않고, 리컴포지션 판단도 안정적이다.
 * 표시용으로 가공한 값(저자 합치기, 할인 여부)은 [Book.toUiModel] 한 곳에서 만든다.
 *
 * 하트를 눌러 저장하려면 도서 전체가 필요하지만 여기에는 표시에 필요한 값만 있다.
 * 원본 [Book] 은 ViewModel 이 들고 있고, 화면은 [id] 만 넘긴다.
 *
 * @param authors 화면에 보일 저자. 여러 명이면 쉼표로 이어 붙인 한 줄이다.
 * @param price 정가(원)
 * @param salePrice 정가와 다른 판매가(원). 같거나 알 수 없으면 null 이라 정가만 보인다.
 */
data class BookUiModel(
    val id: String,
    val status: String,
    val title: String,
    val publisher: String,
    val authors: String,
    val price: Int,
    val salePrice: Int?,
    val thumbnailUrl: String
)

/** 도메인 모델을 화면용으로 바꾼다. */
fun Book.toUiModel(): BookUiModel = BookUiModel(
    id = id,
    status = status,
    title = title,
    publisher = publisher,
    authors = authors.joinToString(", "),
    price = price,
    salePrice = if (isOnSale) salePrice else null,
    thumbnailUrl = thumbnailUrl
)

/** 정가를 화면에 보일 글자로 만든다. (예: 15,000원) */
@Composable
fun BookUiModel.priceText(): String = stringResource(R.string.core_ui_price, formatPrice(price))

/** 판매가를 화면에 보일 글자로 만든다. 정가와 다르지 않으면 null 이다. */
@Composable
fun BookUiModel.salePriceText(): String? = salePrice?.let { stringResource(R.string.core_ui_price, formatPrice(it)) }

/** 천 단위 구분 (예: 15000 → 15,000) */
private fun formatPrice(price: Int): String = NumberFormat.getNumberInstance().format(price)
