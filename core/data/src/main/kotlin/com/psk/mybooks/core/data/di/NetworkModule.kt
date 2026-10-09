package com.psk.mybooks.core.data.di

import com.psk.mybooks.core.data.BuildConfig
import com.psk.mybooks.core.data.remote.BookApi
import com.psk.mybooks.core.data.remote.BookApiErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val KAKAO_BASE_URL: String = "https://dapi.kakao.com/"
private const val AUTHORIZATION_HEADER: String = "Authorization"

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    /**
     * 앱 공용 OkHttpClient. API 키를 붙이지 않는다. (이미지 로더 등이 같이 쓰면 키가 다른 서버로 나간다)
     * 카카오 API 요청만 [provideBookApi] 에서 이 클라이언트를 복제해 인증 헤더를 붙인다.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                // 본문(책 소개가 길다)은 남기지 않고 요청·응답 줄과 헤더만 남기며, 인증 헤더는 가린다
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.HEADERS
                        redactHeader(AUTHORIZATION_HEADER)
                    }
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideBookApi(client: OkHttpClient, json: Json): BookApi {
        // newBuilder 는 연결 풀과 스레드를 공유하므로 클라이언트를 새로 만드는 비용이 없다
        val kakaoClient: OkHttpClient = client.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(AUTHORIZATION_HEADER, "KakaoAK ${BuildConfig.KAKAO_API_KEY}")
                    .build()
                chain.proceed(request)
            }
            // 오류 응답을 BookApiException 으로 바꾼다. 이 클라이언트(BookApi)에만 붙인다.
            .addInterceptor(BookApiErrorHandler())
            .build()

        return Retrofit.Builder()
            .baseUrl(KAKAO_BASE_URL)
            .client(kakaoClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(BookApi::class.java)
    }
}
