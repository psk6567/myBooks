// Android 에 의존하지 않는 순수 Kotlin 모듈. 도메인 모델, Repository 인터페이스, 여러 feature 가 쓰는 UseCase 를 둔다.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Repository 가 Flow<PagingData> 를 돌려주므로 쓰는 쪽에도 보여야 한다
    api(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)
}
