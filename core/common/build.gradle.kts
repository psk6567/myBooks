plugins {
    alias(libs.plugins.mybooks.android.library)
}

android {
    namespace = "com.psk.mybooks.core.common"
}

dependencies {
    // MviViewModel 의 상위 타입이 feature 에 노출되므로 api 로 연다
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.kotlinx.coroutines.android)
}
