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
            // 데이터 계층(구현)은 의존하지 않는다. Repository 는 core:domain 의 인터페이스로만 본다.
            "implementation"(project(":core:domain"))

            "implementation"(libs.library("androidx-lifecycle-viewmodel-compose"))
            "implementation"(libs.library("androidx-lifecycle-runtime-compose"))
            "implementation"(libs.library("androidx-hilt-navigation-compose"))

            "testImplementation"(libs.library("kotlinx-coroutines-test"))
        }
    }
}
