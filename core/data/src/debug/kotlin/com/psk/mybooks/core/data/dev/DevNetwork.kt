package com.psk.mybooks.core.data.dev

import java.util.concurrent.atomic.AtomicInteger

/**
 * 개발자 모드에서 도서 검색 API 호출을 일부러 실패시키거나 느리게 만들기 위한 스위치. debug 빌드에만 존재한다.
 * 이미지 같은 다른 요청에는 영향이 없다. 값은 메모리에만 있어 앱을 다시 시작하면 [DevFault.None] 이다.
 */
object DevNetwork {

    @Volatile
    var fault: DevFault = DevFault.None
        private set

    /** [DevFault.FlakyThenOk] 가 앞으로 더 실패시킬 횟수 */
    internal val flakyRemaining: AtomicInteger = AtomicInteger(0)

    fun select(fault: DevFault) {
        this.fault = fault
        flakyRemaining.set(FLAKY_FAILURES)
    }
}

/** 처음 몇 번만 실패시키고 이후 성공하는 모드가 실패시키는 횟수. 재시도(총 3번 시도)가 마지막 시도에서 성공하도록 2번이다. */
internal const val FLAKY_FAILURES: Int = 2

/** 도서 검색 API 호출에 일으킬 문제 */
enum class DevFault {
    /** 문제 없음 */
    None,

    /** 응답이 오래 걸림(8초). 첫 로딩 스켈레톤과 읽기 시간 제한(10초) 직전의 동작을 본다. */
    Slow,

    /** 시간 초과. 재시도 3번 뒤 "서버 응답이 늦어요" 안내를 본다. */
    Timeout,

    /** 서버 오류(500). 재시도 뒤 서버 오류 안내를 본다. */
    ServerError,

    /** 인증 실패(401). 재시도하지 않고 닫히지 않는 다이얼로그가 뜨는지 본다. */
    Unauthorized,

    /** 연결 실패(네트워크 없음). 재시도 뒤 네트워크 안내를 본다. */
    ConnectionFailure,

    /** 처음 2번만 실패하고 그 뒤 성공. 자동 재시도로 사용자가 모르게 복구되는지 본다. */
    FlakyThenOk
}
