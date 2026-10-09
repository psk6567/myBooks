import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * feature 모듈 공통 설정.
 * 모듈 내부 구조: {Name}Route.kt / ui (+ feature 전용 로직이 있을 때만 domain, data)
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("mybooks.android.library.compose")
        // ViewModel 에 UseCase 등을 주입하기 위해 모든 feature 가 Hilt 를 쓴다
        pluginManager.apply("mybooks.android.hilt")

        dependencies {
            "implementation"(project(":core:common"))
            "implementation"(project(":core:designsystem"))
            "implementation"(project(":core:model"))
            // 데이터 계층(core:data)은 의존하지 않는다. 데이터는 core:domain 의 UseCase 를 거쳐서만 쓴다.
            "implementation"(project(":core:domain"))

            "implementation"(libs.library("androidx-lifecycle-viewmodel-compose"))
            "implementation"(libs.library("androidx-lifecycle-runtime-compose"))
            "implementation"(libs.library("androidx-hilt-navigation-compose"))
            "implementation"(libs.library("androidx-paging-compose"))

            "testImplementation"(libs.library("kotlinx-coroutines-test"))
            "testImplementation"(libs.library("androidx-paging-testing"))
        }
    }
}
