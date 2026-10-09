// 모든 계층(UI, domain, data)이 함께 쓰는 모델. Android 에 의존하지 않는 순수 Kotlin 모듈.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}
