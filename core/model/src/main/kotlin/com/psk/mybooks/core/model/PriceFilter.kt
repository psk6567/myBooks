package com.psk.mybooks.core.model

/**
 * 금액 필터. 판매가([Book.sellingPrice]) 기준이고 [min], [max] 는 그 값도 포함한다. null 이면 그쪽은 제한하지 않는다.
 * 둘 다 null 이면 필터가 없는 것이다.
 */
data class PriceFilter(
    val min: Int? = null,
    val max: Int? = null
) {
    val isActive: Boolean get() = min != null || max != null

    fun matches(price: Int): Boolean = (min == null || price >= min) && (max == null || price <= max)
}
