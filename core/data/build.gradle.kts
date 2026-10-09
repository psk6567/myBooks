import java.util.Properties

plugins {
    alias(libs.plugins.mybooks.android.library)
    alias(libs.plugins.mybooks.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.room)
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

// DB 구조를 버전별 JSON 으로 남겨 두어 구조를 바꿀 때 마이그레이션을 검증할 수 있게 한다
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(project(":core:model"))
    // Repository 가 Flow<PagingData> 를 돌려주므로 쓰는 쪽에도 보여야 한다
    api(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.paging.testing)
}
