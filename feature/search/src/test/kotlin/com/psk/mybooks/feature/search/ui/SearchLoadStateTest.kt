package com.psk.mybooks.feature.search.ui

import androidx.paging.CombinedLoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Paging 이 실제로 내는 로드 상태로, 결과 0건과 "불러오지 않은 빈 목록"이 구분되는지 확인한다. */
class SearchLoadStateTest {

    private class StringPresenter(
        context: CoroutineContext
    ) : PagingDataPresenter<String>(context) {
        override suspend fun presentPagingDataEvent(event: PagingDataEvent<String>) = Unit
    }

    private class FixedSource(
        private val items: List<String>
    ) : PagingSource<Int, String>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> =
            LoadResult.Page(data = items, prevKey = null, nextKey = null)

        override fun getRefreshKey(state: PagingState<Int, String>): Int? = null
    }

    /** 화면(LazyPagingItems)이 하는 것처럼 PagingData 를 수집하고, 그때의 로드 상태와 항목 수를 돌려준다. */
    private fun TestScope.present(data: PagingData<String>): Pair<CombinedLoadStates?, Int> {
        val presenter: StringPresenter = StringPresenter(UnconfinedTestDispatcher(testScheduler))
        // Pager 의 흐름은 끝나지 않으므로 백그라운드에서 수집한다
        backgroundScope.launch { presenter.collectFrom(data) }
        runCurrent()
        return presenter.loadStateFlow.value to presenter.size
    }

    private suspend fun pagedOf(items: List<String>): PagingData<String> =
        Pager(PagingConfig(pageSize = 10, enablePlaceholders = false)) { FixedSource(items) }
            .flow
            .first()

    @Test
    fun `불러온 결과가 0건이면 결과 없음이다`() = runTest {
        val (states, size) = present(pagedOf(emptyList()))

        assertNotNull(states)
        assertEquals(0, size)
        assertTrue(states!!.isNoResult(size))
    }

    @Test
    fun `불러오지 않은 빈 목록(검색어 없음)은 로드 상태가 없어 결과 없음이 아니다`() = runTest {
        val (states, size) = present(PagingData.empty())

        // 상태가 없으면 화면은 "불러오는 중"인 초기 상태로 둔다. 결과 없음으로 보이면 글자를 입력한 직후 깜빡인다.
        assertNull(states)
        assertEquals(0, size)
    }

    @Test
    fun `더 불러올 페이지가 있으면 빈 페이지여도 결과 없음이 아니다`() = runTest {
        val data: PagingData<String> = Pager(PagingConfig(pageSize = 10, enablePlaceholders = false)) {
            object : PagingSource<Int, String>() {
                // 첫 페이지는 비어 있지만 다음 페이지가 있다. 다음 페이지는 응답이 오지 않는 "불러오는 중"으로 둔다.
                // (계속 빈 페이지를 주면 Paging 이 항목이 모자라다고 보고 끝없이 다음 페이지를 요청한다)
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> = when (params) {
                    is LoadParams.Append -> awaitCancellation()
                    else -> LoadResult.Page(data = emptyList(), prevKey = null, nextKey = 2)
                }

                override fun getRefreshKey(state: PagingState<Int, String>): Int? = null
            }
        }.flow.first()

        val (states, size) = present(data)

        assertFalse(states!!.isNoResult(size))
    }

    @Test
    fun `결과가 있으면 결과 없음이 아니다`() = runTest {
        val (states, size) = present(pagedOf(listOf("a", "b")))

        assertEquals(2, size)
        assertFalse(states!!.isNoResult(size))
    }
}
