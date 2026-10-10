package com.psk.mybooks.dev

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import java.lang.ref.WeakReference

/** 제목을 이 횟수만큼 이 시간 안에 누르면 개발자 모드가 열린다. */
private const val REQUIRED_TAPS: Int = 7
private const val TAP_WINDOW_MILLIS: Long = 3_000

/**
 * 개발자 모드의 입구. 메인 화면의 제목을 짧은 시간에 여러 번 누르면 [DevActivity] 를 연다. debug 빌드에만 있다.
 * (release 소스셋에 같은 이름의 빈 구현이 있어 MainActivity 는 빌드 종류를 신경 쓰지 않는다)
 */
object DevEntry {

    private var tapCount: Int = 0
    private var firstTapAt: Long = 0
    private var mainActivity: WeakReference<Activity>? = null

    fun titleClick(activity: Activity): () -> Unit = {
        mainActivity = WeakReference(activity)
        val now: Long = SystemClock.elapsedRealtime()
        if (now - firstTapAt > TAP_WINDOW_MILLIS) {
            tapCount = 0
            firstTapAt = now
        }
        tapCount++
        if (tapCount >= REQUIRED_TAPS) {
            tapCount = 0
            activity.startActivity(Intent(activity, DevActivity::class.java))
        }
    }

    /** 메인 화면을 다시 만든다. (Warm start: 프로세스는 그대로, Activity 만 새로) */
    internal fun recreateMain() {
        mainActivity
            ?.get()
            ?.recreate()
    }
}
