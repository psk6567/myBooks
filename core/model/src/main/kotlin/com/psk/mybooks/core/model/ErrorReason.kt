package com.psk.mybooks.core.model

/**
 * 오류의 중요도. 화면이 어떻게 알릴지를 정한다.
 * [Low] 는 잠깐 보였다 사라지는 안내(스낵바), [High] 는 사용자가 확인해야 하는 안내(다이얼로그)다.
 */
enum class ErrorSeverity { Low, High }

/**
 * 사용자에게 알릴 오류의 종류. 화면은 HTTP 상태나 에러 코드를 모르고 이것만 본다.
 * 중요도는 기획이 정해지기 전까지 "다시 해도 같아서 사용자가 알아야 하는 인증·권한 오류만 High, 나머지는 Low" 로 정했다.
 */
enum class ErrorReason(
    val severity: ErrorSeverity
) {
    /** 연결 실패, 시간 초과 등 응답을 받지 못함 */
    Network(ErrorSeverity.Low),

    /** 인증 실패(앱 키 문제). 다시 해도 같다. */
    Unauthorized(ErrorSeverity.High),

    /** 권한 없음이나 제재. 다시 해도 같다. */
    Forbidden(ErrorSeverity.High),

    /** 잘못된 요청 */
    InvalidRequest(ErrorSeverity.Low),

    /** 호출 횟수 초과 */
    QuotaExceeded(ErrorSeverity.Low),

    /** 서버 오류 */
    Server(ErrorSeverity.Low),

    /** 위에 해당하지 않는 오류 */
    Unknown(ErrorSeverity.Low)
}

/** 데이터 계층이 화면에 알릴 오류를 [reason] 으로 분류해 던지는 예외. 원래 예외는 [cause] 에 남는다. */
class AppException(
    val reason: ErrorReason,
    cause: Throwable? = null
) : Exception("AppException($reason)", cause)

/** 어떤 예외든 화면에 알릴 종류로 바꾼다. [AppException] 이 아니면(예: 저장소 오류) [ErrorReason.Unknown] 이다. */
fun Throwable.toErrorReason(): ErrorReason = (this as? AppException)?.reason ?: ErrorReason.Unknown
