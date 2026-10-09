plugins {
    alias(libs.plugins.mybooks.android.feature)
    // 이동 경로(DetailDestination)와 이동 인자(BookUiModel)를 JSON 으로 직렬화한다
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.psk.mybooks.feature.detail"
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // 테스트에서 가짜 FavoriteRepository 로 UseCase 를 만든다. 앱 코드는 데이터 계층을 모른다.
    testImplementation(project(":core:data"))
}
