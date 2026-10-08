plugins {
    alias(libs.plugins.mybooks.android.feature)
}

android {
    namespace = "com.psk.mybooks.feature.main"
}

// 다른 feature 를 의존할 수 있는 건 main 뿐이다. (탭 화면 연결 담당)
dependencies {
    implementation(project(":feature:search"))
    implementation(project(":feature:favorite"))
}
