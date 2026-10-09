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
}
