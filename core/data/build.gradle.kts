import java.util.Properties

plugins {
    alias(libs.plugins.mybooks.android.library)
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
    api(project(":core:model"))

    // 이후 Repository 가 Flow<PagingData> 를 공개하면 feature 도 이 타입을 쓰게 되므로 api 로 둔다
    api(libs.androidx.paging.common)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.paging.testing)
}
