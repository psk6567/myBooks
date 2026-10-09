plugins {
    alias(libs.plugins.mybooks.android.library.compose)
    // BookUiModel 을 화면 이동 인자(JSON)로 넘긴다
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.psk.mybooks.core.ui"
}

dependencies {
    // 스낵바, 다이얼로그를 그리는 디자인 시스템과 오류 종류(ErrorReason)
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
}
