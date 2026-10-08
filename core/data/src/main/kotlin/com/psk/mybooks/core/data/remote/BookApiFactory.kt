package com.psk.mybooks.core.data.remote

import com.psk.mybooks.core.data.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val BASE_URL: String = "https://dapi.kakao.com/"
private const val AUTHORIZATION_HEADER: String = "Authorization"

// TODO: Hilt 를 붙이면 이 함수를 @Provides 모듈로 옮긴다. (OkHttpClient 는 이미지 로더와 공유)
internal fun createBookApi(apiKey: String = BuildConfig.KAKAO_API_KEY, baseUrl: String = BASE_URL): BookApi {
    val json: Json = Json { ignoreUnknownKeys = true }
    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().header(AUTHORIZATION_HEADER, "KakaoAK $apiKey").build())
        }
        .apply {
            if (BuildConfig.DEBUG) {
                // 본문(책 소개가 길다)은 남기지 않고 요청·응답 줄과 헤더만 남기며, 키는 가린다
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.HEADERS
                        redactHeader(AUTHORIZATION_HEADER)
                    }
                )
            }
        }
        .build()

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(BookApi::class.java)
}
