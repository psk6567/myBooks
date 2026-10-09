package com.psk.mybooks.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI 기반 ViewModel.
 *
 * - [S] State : 화면이 그리는 모든 상태. 화면은 [state] 만 보고 그린다.
 * - [I] Intent : 사용자 행동 등 화면이 ViewModel 에 보내는 입력. [onIntent] 로만 들어온다.
 * - [E] Effect : 한 번만 처리할 일(토스트, 화면 이동 등). [effect] 로 내보내고, 없으면 `Nothing`.
 *
 * - 오류 : 화면별 Effect 와 따로, 모든 화면이 같은 방식으로 사용자에게 알릴 오류를 [error] 로 내보낸다. [sendError] 로 보낸다.
 *
 * 상태 변경은 [reduce] 로만 한다. (Intent → reduce → State)
 */
abstract class MviViewModel<S : Any, I : Any, E : Any>(
    initialState: S
) : ViewModel() {

    private val _state: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect: Channel<E> = Channel(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    private val _error: Channel<Throwable> = Channel(Channel.BUFFERED)
    private val lastError: AtomicReference<Throwable?> = AtomicReference(null)

    /**
     * 사용자에게 알릴 오류. 한 번만 전달되고, 구독자가 없으면 쌓아 두었다가 구독하면 전달한다.
     * 모든 화면이 같은 방식으로 처리하므로 화면별 Effect 가 아니라 이 흐름을 쓴다.
     */
    val error: Flow<Throwable> = _error.receiveAsFlow()

    abstract fun onIntent(intent: I)

    protected fun reduce(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }

    /**
     * 사용자에게 알릴 오류를 [error] 로 보낸다. 어떤 예외든 보낼 수 있고, 화면이 알릴 종류로 바꾼다.
     *
     * 직전에 보낸 것과 **같은 예외 객체**면 보내지 않는다. 화면이 상태(예: Paging 로드 상태)를 다시 읽을 때마다
     * 같은 오류를 알려 와도(화면 회전 등) 알림이 중복되지 않게 하려는 것이다.
     * 새로 발생한 예외는 항상 새 객체라서 정상적인 오류가 걸러지지는 않는다.
     */
    protected fun sendError(throwable: Throwable) {
        if (lastError.getAndSet(throwable) === throwable) return
        viewModelScope.launch { _error.send(throwable) }
    }
}
