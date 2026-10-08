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

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.kotlinx.coroutines.test)
}
