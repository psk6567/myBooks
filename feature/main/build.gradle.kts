plugins {
    alias(libs.plugins.mybooks.android.feature)
    // 홈의 이동 경로(HomeDestination)를 직렬화한다
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.psk.mybooks.feature.main"
}

// 다른 feature 를 의존할 수 있는 건 main 뿐이다. (탭 화면 연결과 화면 이동 담당)
dependencies {
    implementation(project(":feature:search"))
    implementation(project(":feature:favorite"))
    implementation(project(":feature:detail"))

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
