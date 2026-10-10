package com.psk.mybooks.core.data.remote

import com.psk.mybooks.core.model.AppException
import com.psk.mybooks.core.model.ErrorReason
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 카카오 도서 검색 API 가 오류로 응답했을 때의 예외. [BookApiErrorHandler] 가 응답을 이 종류들로 나눈다.
 *
 * OkHttp 가 비동기 호출 중 IOException 이 아닌 예외를 받으면 앱이 종료될 수 있어서 IOException 을 상속한다.
 * 서버가 준 `message` 는 담지 않는다. 키 형식이 틀리면 `wrong appKey(<키 값>) format` 처럼 키를 되돌려 줘서,
 * 로그나 화면에 키가 남을 수 있기 때문이다.
 *
 * @param httpStatus HTTP 상태 코드
 * @param errorCode 카카오 공통 에러 코드(`code`, 예: -401). 응답에 없으면 null
 * @param errorType 검색 API 의 `errorType`(예: AccessDeniedError). 응답에 없으면 null
 */
internal sealed class BookApiException(
    val httpStatus: Int,
    val errorCode: Int?,
    val errorType: String?
) : IOException("HTTP $httpStatus" + (errorCode?.let { " code=$it" } ?: "") + (errorType?.let { " $it" } ?: "")) {

    /** 키가 없거나 틀림 (401, -401). 재시도해도 같으므로 설정(앱 키)을 확인해야 한다. */
    class Unauthorized(
        httpStatus: Int,
        errorCode: Int?,
        errorType: String?
    ) : BookApiException(httpStatus, errorCode, errorType)

    /** 이 앱이나 API 를 쓸 권한이 없음 (403, -3~-6, -12, -13). 앱 설정이나 제재 상태를 확인해야 한다. */
    class Forbidden(
        httpStatus: Int,
        errorCode: Int?,
        errorType: String?
    ) : BookApiException(httpStatus, errorCode, errorType)

    /** 요청이 잘못됨 (400, 404 등: 필수 값 누락, 잘못된 헤더·경로, 종료된 API). 앱을 고쳐야 하는 문제다. */
    class InvalidRequest(
        httpStatus: Int,
        errorCode: Int?,
        errorType: String?
    ) : BookApiException(httpStatus, errorCode, errorType)

    /** 허용된 요청 횟수를 넘음 (429, -10, -11). 잠시 뒤에 다시 해야 한다. */
    class QuotaExceeded(
        httpStatus: Int,
        errorCode: Int?,
        errorType: String?
    ) : BookApiException(httpStatus, errorCode, errorType)

    /** 서버 쪽 문제 (5xx, -1, -7). 다시 시도하면 될 수 있다. */
    class ServerError(
        httpStatus: Int,
        errorCode: Int?,
        errorType: String?
    ) : BookApiException(httpStatus, errorCode, errorType)

    /** 위에 해당하지 않는 오류 */
    class Unknown(
        httpStatus: Int,
        errorCode: Int?,
        errorType: String?
    ) : BookApiException(httpStatus, errorCode, errorType)
}

/**
 * 도서 검색 중 생긴 예외를 화면에 알릴 [AppException] 으로 바꾼다. 원래 예외는 원인으로 남긴다.
 * API 오류는 [BookApiException] 종류대로, 시간 초과([SocketTimeoutException])는 [ErrorReason.Timeout], 그 밖에 응답을 받지 못한
 * IOException(연결 실패, 끊김 등)은 [ErrorReason.Network] 로 나눈다.
 */
internal fun Throwable.toAppException(): AppException {
    if (this is AppException) return this
    val reason: ErrorReason = when (this) {
        is BookApiException.Unauthorized -> ErrorReason.Unauthorized

        is BookApiException.Forbidden -> ErrorReason.Forbidden

        is BookApiException.InvalidRequest -> ErrorReason.InvalidRequest

        is BookApiException.QuotaExceeded -> ErrorReason.QuotaExceeded

        is BookApiException.ServerError -> ErrorReason.Server

        is BookApiException.Unknown -> ErrorReason.Unknown

        // BookApiException 도 IOException 이라 위에서 먼저 걸러야 한다
        is SocketTimeoutException -> ErrorReason.Timeout

        is IOException -> ErrorReason.Network

        else -> ErrorReason.Unknown
    }
    return AppException(reason, this)
}
