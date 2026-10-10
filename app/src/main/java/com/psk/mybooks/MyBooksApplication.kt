package com.psk.mybooks

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath

@HiltAndroidApp
class MyBooksApplication :
    Application(),
    SingletonImageLoader.Factory {

    /**
     * 앱 전체가 쓰는 이미지 로더. (Coil 이 처음 이미지를 그릴 때 한 번 만든다)
     *
     * - 메모리 캐시: 디코딩된 비트맵. 목록을 위아래로 오갈 때 다시 디코딩하지 않는다.
     * - 디스크 캐시: 원본 응답. 도서 표지 URL 은 내용이 바뀌지 않으므로 서버 Cache-Control 과 상관없이 저장해 둔다. (Coil 3 기본 동작)
     *   cacheDir 아래라 저장 공간이 부족하면 OS 가 지울 수 있다.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader
        .Builder(context)
        .memoryCache {
            MemoryCache
                .Builder()
                .maxSizePercent(context, IMAGE_MEMORY_CACHE_PERCENT)
                .build()
        }.diskCache {
            DiskCache
                .Builder()
                .directory(
                    context.cacheDir
                        .resolve("image_cache")
                        .toOkioPath()
                ).maxSizeBytes(IMAGE_DISK_CACHE_BYTES)
                .build()
        }.crossfade(true)
        .build()
}

/** 앱이 쓸 수 있는 메모리 중 이미지 캐시 비율 */
private const val IMAGE_MEMORY_CACHE_PERCENT: Double = 0.25

/** 표지 한 장이 수십 KB 라 50MB 면 수천 장을 담는다 */
private const val IMAGE_DISK_CACHE_BYTES: Long = 50L * 1024 * 1024
