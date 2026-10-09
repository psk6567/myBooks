// UseCase 를 둔다. 여러 feature 가 쓰는 화면 밖의 규칙(빈 검색어 처리, 실행 스레드 등)을 Repository 위에서 처리한다.
plugins {
    alias(libs.plugins.mybooks.android.library)
    alias(libs.plugins.mybooks.android.hilt)
}

android {
    namespace = "com.psk.mybooks.core.domain"
}

dependencies {
    api(project(":core:model"))
    // UseCase 가 Flow<PagingData> 를 돌려주므로 쓰는 쪽에도 보여야 한다
    api(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)
    // UseCase 가 Repository 를 쓴다. implementation 이라 feature 에는 데이터 계층이 보이지 않는다.
    implementation(project(":core:data"))
    // 실행 Dispatcher 를 구분하는 @IoDispatcher
    implementation(project(":core:common"))

    testImplementation(libs.kotlinx.coroutines.test)
}
