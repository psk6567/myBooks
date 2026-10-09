plugins {
    alias(libs.plugins.mybooks.android.feature)
}

android {
    namespace = "com.psk.mybooks.feature.favorite"
}

dependencies {
    // 테스트에서 가짜 FavoriteRepository 로 UseCase 를 만든다. 앱 코드는 데이터 계층을 모른다.
    testImplementation(project(":core:data"))
}
