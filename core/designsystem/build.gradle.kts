plugins {
    alias(libs.plugins.mybooks.android.library.compose)
}

android {
    namespace = "com.psk.mybooks.core.designsystem"
}

dependencies {
    // NetworkImage 의 이미지 로딩
    implementation(libs.coil.compose)

    testImplementation(libs.kotlinx.coroutines.test)
}
