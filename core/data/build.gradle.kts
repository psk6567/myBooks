import java.util.Properties

plugins {
    alias(libs.plugins.mybooks.android.library)
    alias(libs.plugins.mybooks.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// API 키는 저장소에 올리지 않고 local.properties 에 둔다. (KAKAO_API_KEY=...)
val localProperties: Properties = Properties().apply {
    val file: File = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val kakaoApiKey: String = localProperties.getProperty("KAKAO_API_KEY", "")

android {
    namespace = "com.psk.mybooks.core.data"

    defaultConfig {
        buildConfigField("String", "KAKAO_API_KEY", "\"$kakaoApiKey\"")
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Repository 인터페이스를 가진 도메인 계층. 구현(이 모듈)이 도메인을 의존한다.
    api(project(":core:domain"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.paging.testing)
}
