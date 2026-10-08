# MyBooks

## 개발 환경

| 항목 | 버전 |
|---|---|
| Gradle | 9.4.1 |
| Android Gradle Plugin (AGP) | 9.2.1 |
| Kotlin | 2.4.0 |
| KSP | 2.3.10 |
| JDK | 17 |
| compileSdk / targetSdk | 37 |
| minSdk | 24 |

## 주요 라이브러리

| 용도 | 라이브러리 | 버전 |
|---|---|---|
| DI | Hilt (+ hilt-navigation-compose) | 2.60.1 (1.4.0) |
| 로컬 DB | Room | 2.8.5 |
| 네트워크 | Retrofit | 3.0.0 |
| HTTP 클라이언트 | OkHttp (+ logging-interceptor) | 5.5.0 |
| 비동기 | kotlinx.coroutines (android, test) | 1.11.0 |
| 직렬화 | kotlinx.serialization-json | 1.11.0 |
| UI | Jetpack Compose (BOM 2026.02.01) + Material3 | - |

버전은 모두 [gradle/libs.versions.toml](gradle/libs.versions.toml)에서 관리합니다.
