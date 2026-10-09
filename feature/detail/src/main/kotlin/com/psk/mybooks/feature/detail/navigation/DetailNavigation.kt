package com.psk.mybooks.feature.detail.navigation

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.feature.detail.ui.DetailRoute
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 상세 화면의 이동 경로. 보여 줄 도서 정보 전체를 이동 인자로 받는다.
 *
 * NOTE: 앱 스킴(예: `mybooks://book/{bookId}`)으로 상세를 바로 여는 것은 아직 지원하지 않는다. 하려면 아래가 필요하다.
 *  1. 이 경로가 도서 정보 전체를 인자로 받으므로 bookId 만 있는 스킴으로는 열 수 없다. 카카오 도서 API 에는 id 로
 *     조회하는 기능이 없고 검색 결과도 저장하지 않는다. bookId 로 열려면 도서를 찾을 수단이 먼저 필요하다.
 *     (즐겨찾기 DB 조회는 즐겨찾기한 도서만, 최근 검색 결과 메모리 캐시는 앱이 새로 켜지면 비어 있다,
 *     ISBN 을 담아 `target=isbn` 으로 다시 검색하는 방법도 있다)
 *  2. 그 뒤에 `composable<DetailDestination>` 에 `navDeepLink` 를 붙이고, MainActivity 에 스킴 intent-filter 를 추가한다.
 *  3. 앱이 새로 켜져 상세로 바로 들어오면 뒤로가기가 앱을 종료한다. 홈을 백스택 아래에 둘지 정해야 한다.
 */
@Serializable
data class DetailDestination(
    val book: BookUiModel
)

/** NavHost 에 상세 화면을 등록한다. @param onBack 뒤로가기를 눌렀을 때 */
fun NavGraphBuilder.detailScreen(onBack: () -> Unit) {
    composable<DetailDestination>(typeMap = mapOf(typeOf<BookUiModel>() to BookUiModelNavType)) {
        DetailRoute(onBack = onBack)
    }
}

/**
 * 이동 인자 이름. [DetailDestination.book] 의 속성 이름이다. Navigation 이 인자를 이 이름으로 담고,
 * [BookUiModelNavType] 이 JSON 문자열로 넣는다.
 */
private val BOOK_ARG: String = DetailDestination::book.name

/** 상세 화면의 ViewModel 이 이동 인자(보여 줄 도서)를 읽는다. */
internal fun SavedStateHandle.readBook(): BookUiModel =
    Json.decodeFromString<BookUiModel>(requireNotNull(get<String>(BOOK_ARG)) { "상세 화면의 도서 인자가 없다" })

/** [book] 의 상세 화면으로 이동한다. */
fun NavController.navigateToDetail(book: BookUiModel) {
    navigate(DetailDestination(book))
}

/**
 * [BookUiModel] 을 이동 인자로 넘기기 위한 타입. JSON 문자열로 바꿔 경로에 싣고, 경로에 안전하도록 URI 인코딩한다.
 * 표지 URL 이나 소개처럼 `/`, `?`, `&` 가 들어 있는 글자도 깨지지 않는다.
 */
private val BookUiModelNavType: NavType<BookUiModel> = object : NavType<BookUiModel>(isNullableAllowed = false) {

    override fun get(bundle: Bundle, key: String): BookUiModel? =
        bundle.getString(key)?.let { Json.decodeFromString<BookUiModel>(it) }

    override fun parseValue(value: String): BookUiModel = Json.decodeFromString<BookUiModel>(value)

    override fun serializeAsValue(value: BookUiModel): String = Uri.encode(Json.encodeToString(value))

    override fun put(bundle: Bundle, key: String, value: BookUiModel) {
        bundle.putString(key, Json.encodeToString(value))
    }
}
