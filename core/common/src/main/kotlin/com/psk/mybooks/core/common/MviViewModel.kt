package com.psk.mybooks.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * 상태 변경은 [reduce] 로만 한다. (Intent → reduce → State)
 */
abstract class MviViewModel<S : Any, I : Any, E : Any>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    abstract fun onIntent(intent: I)

    protected fun reduce(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
