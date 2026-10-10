package com.psk.mybooks.core.model

/**
 * 오류의 중요도. 화면이 어떻게 알릴지를 정한다.
 * [Low] 는 잠깐 보였다 사라지는 안내(스낵바), [High] 는 사용자가 확인해야 하는 안내(다이얼로그)다.
 */
enum class ErrorSeverity { Low, High }

/**
 * 사용자에게 알릴 오류의 종류. 화면은 HTTP 상태나 에러 코드를 모르고 이것만 본다.
 * 중요도는 기획이 정해지기 전까지 "다시 해도 같아서 사용자가 알아야 하는 인증·권한 오류만 High, 나머지는 Low" 로 정했다.
 *
 * @param severity 중요도. 어떻게 알릴지를 정한다.
 * @param isRetryable 사용자가 "다시 시도"를 눌러 볼 만한지. 일시적인 문제만 true 이고, 앱 설정이나 권한 문제처럼
 * 다시 해도 같은 오류는 false 다.
 */
enum class ErrorReason(
    val severity: ErrorSeverity,
    val isRetryable: Boolean
) {
    /** 연결 실패, 끊김 등으로 응답을 받지 못함 */
    Network(ErrorSeverity.Low, isRetryable = true),

    /** 연결은 됐지만 응답이 너무 늦음(시간 초과). 끊긴 것이 아니라 느린 것이라 네트워크 오류와 문구를 달리한다. */
    Timeout(ErrorSeverity.Low, isRetryable = true),

    /** 인증 실패(앱 키 문제). 다시 해도 같다. */
    Unauthorized(ErrorSeverity.High, isRetryable = false),

    /** 권한 없음이나 제재. 다시 해도 같다. */
    Forbidden(ErrorSeverity.High, isRetryable = false),

    /** 잘못된 요청 */
    InvalidRequest(ErrorSeverity.Low, isRetryable = false),

    /** 호출 횟수 초과. 자동으로 다시 하지는 않지만 잠시 뒤에 사용자가 다시 해 볼 수 있다. */
    QuotaExceeded(ErrorSeverity.Low, isRetryable = true),

    /** 서버 오류 */
    Server(ErrorSeverity.Low, isRetryable = true),

    /** 위에 해당하지 않는 오류 */
    Unknown(ErrorSeverity.Low, isRetryable = false)
}

/** 데이터 계층이 화면에 알릴 오류를 [reason] 으로 분류해 던지는 예외. 원래 예외는 [cause] 에 남는다. */
class AppException(
    val reason: ErrorReason,
    cause: Throwable? = null
) : Exception("AppException($reason)", cause)

/** 어떤 예외든 화면에 알릴 종류로 바꾼다. [AppException] 이 아니면(예: 저장소 오류) [ErrorReason.Unknown] 이다. */
fun Throwable.toErrorReason(): ErrorReason = (this as? AppException)?.reason ?: ErrorReason.Unknown
