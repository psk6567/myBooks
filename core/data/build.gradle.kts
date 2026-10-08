plugins {
    alias(libs.plugins.mybooks.android.library)
}

android {
    namespace = "com.psk.mybooks.core.data"
}

dependencies {
    api(project(":core:model"))
}
