package com.psk.mybooks.dev

import android.app.Activity

/** release 에는 개발자 모드가 없다. (debug 소스셋에 같은 이름의 실제 구현이 있다) */
object DevEntry {
    fun titleClick(activity: Activity): (() -> Unit)? = null
}
