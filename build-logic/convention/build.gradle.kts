plugins {
    `kotlin-dsl`
}

group = "com.psk.mybooks.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "mybooks.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "mybooks.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "mybooks.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
    }
}
