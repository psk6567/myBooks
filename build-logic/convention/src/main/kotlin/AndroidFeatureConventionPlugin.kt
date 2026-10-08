import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * feature 모듈 공통 설정.
 * 모듈 내부 구조: {Name}Route.kt / ui (+ feature 전용 로직이 있을 때만 domain, data)
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("mybooks.android.library.compose")

        dependencies {
            "implementation"(project(":core:common"))
            "implementation"(project(":core:designsystem"))
            "implementation"(project(":core:model"))
            "implementation"(project(":core:data"))

            "implementation"(libs.library("androidx-lifecycle-viewmodel-compose"))
        }
    }
}
