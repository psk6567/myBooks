// Android 에 의존하지 않는 순수 Kotlin 모듈
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}
