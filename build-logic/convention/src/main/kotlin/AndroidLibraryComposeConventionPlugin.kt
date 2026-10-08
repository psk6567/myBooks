import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Compose 를 쓰는 라이브러리 모듈 (core:designsystem 등) */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("mybooks.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            "implementation"(platform(libs.library("androidx-compose-bom")))
            "implementation"(libs.library("androidx-compose-ui"))
            "implementation"(libs.library("androidx-compose-ui-tooling-preview"))
            "implementation"(libs.library("androidx-compose-material3"))
            "debugImplementation"(libs.library("androidx-compose-ui-tooling"))
        }
    }
}
