# MyBooks

## 개발 환경

| 항목                          | 버전     |
|-----------------------------|--------|
| Gradle                      | 9.4.1  |
| Android Gradle Plugin (AGP) | 9.2.1  |
| Kotlin                      | 2.4.0  |
| KSP                         | 2.3.10 |
| JDK                         | 17     |
| compileSdk / targetSdk      | 37     |
| minSdk                      | 24     |

## 주요 라이브러리

| 용도         | 라이브러리                                        | 버전             |
|------------|----------------------------------------------|----------------|
| DI         | Hilt (+ hilt-navigation-compose)             | 2.60.1 (1.4.0) |
| 로컬 DB      | Room                                         | 2.8.5          |
| 네트워크       | Retrofit                                     | 3.0.0          |
| HTTP 클라이언트 | OkHttp (+ logging-interceptor)               | 5.5.0          |
| 비동기        | kotlinx.coroutines (android, test)           | 1.11.0         |
| 직렬화        | kotlinx.serialization-json                   | 1.11.0         |
| UI         | Jetpack Compose (BOM 2026.02.01) + Material3 | -              |

버전은 모두 [gradle/libs.versions.toml](gradle/libs.versions.toml)에서 관리합니다.

## 모듈 구조

```
app                     Application, MainActivity
build-logic             Convention Plugin (모듈 공통 Gradle 설정)
core
├── common              공통 유틸
├── designsystem        색상·컴포넌트 등 디자인 시스템
├── model               공유 도메인 모델 (순수 Kotlin)
└── data                Repository(인터페이스 + internal 구현), API, DB
feature
├── main                하단 탭 + 화면 이동 연결
├── search              검색
├── favorite            즐겨찾기
└── detail              도서 상세
```

- **배치 기준: 두 개 이상의 feature가 쓰면 core, 한 feature만 쓰면 그 feature 안에 둡니다.**
- 이 앱의 데이터(도서, 즐겨찾기)는 세 화면이 모두 공유하므로 `core:model`, `core:data`에 둡니다.
- `core:data`의 구현 클래스는 `internal`이라 feature에서는 Repository 인터페이스만 보입니다.
- feature는 core만 의존하고, 다른 feature를 의존하지 않습니다. 화면 간 이동은 `feature:main`이 연결합니다.
- 모듈 공통 설정(SDK 버전, Java 버전, Compose)은 `build-logic`의 Convention Plugin이 담당합니다.

### feature 모듈 내부 구조

feature는 기본적으로 UI 레이어(Screen, ViewModel, UI 모델)만 가집니다. 모듈로 나누는 목적은 레이어 분리가 아니라 화면 단위 격리(빌드 캐시,
`internal` 캡슐화)입니다.

```
feature/{name}/src/main/kotlin/com/psk/mybooks/feature/{name}/
├── ui/
│   ├── {Name}Screen.kt     {Name}Route(ViewModel 연결) + {Name}Screen(UI)
│   └── {Name}ViewModel.kt  상태 관리 (internal)
├── domain/                 (선택) 이 feature만 쓰는 UseCase
└── data/                   (선택) 이 feature만 쓰는 데이터 소스
```

```kotlin
// feature/{name}/build.gradle.kts
plugins {
    alias(libs.plugins.mybooks.android.feature)
}

android {
    namespace = "com.psk.mybooks.feature.{name}"
}
```

### Convention Plugin

| 플러그인                              | 용도                               |
|-----------------------------------|----------------------------------|
| `mybooks.android.library`         | Android 라이브러리 모듈 공통 설정           |
| `mybooks.android.library.compose` | 위 설정 + Compose                   |
| `mybooks.android.feature`         | 위 설정 + core 모듈 4개, ViewModel 의존성 |

새 모듈은 기존 플러그인으로 충분하며, 새로운 종류의 모듈이 필요할 때만 `build-logic`에 추가합니다.

## 아키텍처 패턴: MVI

모든 feature의 ViewModel은 `core:common`의 `MviViewModel<State, Intent, Effect>`를 상속합니다.

```
화면 ──Intent──▶ ViewModel ──reduce──▶ State ──▶ 화면
                    └──────Effect(일회성)──────▶ 화면
```

| 구성 | 역할 | 규칙 |
|---|---|---|
| **State** | 화면이 그리는 모든 상태 | 불변 `data class`, 화면은 `state`만 보고 그린다 |
| **Intent** | 사용자 행동 등 화면 → ViewModel 입력 | `sealed interface`, `onIntent()`로만 들어온다 |
| **Effect** | 한 번만 처리할 일(토스트, 이동 등) | 없으면 `Nothing` |

- 상태 변경은 `reduce { copy(...) }`로만 합니다.
- State/Intent/Effect는 화면 파일 옆의 `{Name}Contract.kt`에 둡니다.
- `Screen`은 `state`와 `onIntent`만 받습니다. ViewModel을 직접 모르므로 Preview와 테스트가 쉽습니다.
- ViewModel은 Android 의존 없이 JUnit만으로 테스트합니다. (예: `MainViewModelTest`)

### main 화면의 탭 이동
`MainViewModel`이 선택된 탭을 상태로 가지고, 탭 클릭과 Pager 스와이프가 모두 `SelectTab` Intent로 들어옵니다.
상태(선택된 탭)가 Pager 이동의 기준이고, Pager가 넘어가면 다시 Intent로 상태를 맞춥니다.

### BottomSheet 띄우기
화면에서 시트를 한 줄로 선언하고, 반환된 손잡이로 엽니다. 화면마다 `if`나 열림 상태를 두지 않습니다.

```kotlin
val sortSheet: BottomSheetHandle = rememberBottomSheet { dismiss ->
    SortOptions(selected = state.sort) { sort ->
        onIntent(MainIntent.ChangeSort(sort))
        dismiss()   // 닫힘 애니메이션 후 닫힘
    }
}
SortChip(onClick = { sortSheet.show() })
```

- 시트 열림 여부는 ViewModel이 아니라 UI가 관리합니다(`rememberSaveable`). ViewModel은 선택 결과만 Intent로 받습니다.
- 화면 회전과 프로세스 종료 후에도 열린 상태가 복원됩니다.
- 시트 내용은 매번 다시 그려지므로 열린 동안 바뀐 상태도 반영됩니다.
- ViewModel이 열 시점을 정해야 하면 Effect를 보내고, 화면이 그 Effect를 받아 `show()`를 호출합니다.

## 디자인 시스템 - 색상

색상은 두 단계로 나눕니다.

| 단계 | 파일 | 이름 예시 | 공개 범위 |
|---|---|---|---|
| Color Token (팔레트) | `ColorTokens.kt` | `Red55`, `Gray92` (계열 + 톤) | `internal` (designsystem 내부) |
| Semantic Color (용도) | `MbColors.kt` | `textPrimary`, `favorite`, `border` | public |

- 화면과 컴포넌트는 **`MyBooksTheme.colors.xxx`만** 사용합니다. 토큰과 `MaterialTheme.colorScheme`은 직접 쓰지 않습니다.
- 토큰은 `internal`이라 feature 모듈에서는 쓰는 순간 컴파일 에러가 납니다.
- Material 컴포넌트가 같은 톤을 내도록 `Theme.kt`에서 Material `ColorScheme`에 매핑합니다.
- 브랜드 컬러 `#F83949`를 [Material Color Utilities](https://github.com/material-foundation/material-color-utilities)에 넣어 팔레트를 생성했습니다. 배경과 글자는 무채색(Gray)을 씁니다.
- 라이트/다크 모드를 지원합니다. 다이내믹 컬러는 쓰지 않습니다.

## 디자인 시스템 - 타이포그래피

- 폰트: [여기어때 잘난체 2](https://www.gccompany.co.kr) (개인·기업 무료, 수정·재배포 허용) — `core/designsystem/src/main/res/font/jalnan2.ttf`
- 스케일: 10~24sp (2 단위) × `normal` / `bold` → `MyBooksTheme.typography.normal14`, `bold20` 등
- 줄 높이는 글자 크기의 1.4배입니다.
- 폰트 파일이 Regular 한 가지라 `bold`는 Compose가 굵기를 합성해서 그립니다.
- 화면과 컴포넌트는 `MyBooksTheme.typography.xxx`만 사용합니다. `fontSize`를 직접 지정하지 않습니다.
- Material 컴포넌트도 같은 폰트를 쓰도록 `Theme.kt`에서 Material `Typography`에 매핑합니다.

## 신규 모듈 추가 (Claude Skill)

모듈 추가 규칙은 프로젝트
skill [.claude/skills/mybooks-new-module/SKILL.md](.claude/skills/mybooks-new-module/SKILL.md)에 정의되어
있습니다. Claude Code로 새 feature/core 모듈을 만들면 이 skill이 위 구조를 그대로 따르도록 안내합니다.

수동으로 추가할 때의 절차도 같습니다.

1. 위 구조대로 폴더와 파일을 만든다.
2. `settings.gradle.kts`에 `include(":feature:{name}")`을 추가한다.
3. 화면이 탭/이동 대상이면 `feature:main`에서 연결한다.
4. `./gradlew :feature:{name}:assembleDebug`로 빌드를 확인한다.

## 코드 스타일

[ktlint](https://pinterest.github.io/ktlint/)(Android Studio 스타일)
를 [Spotless](https://github.com/diffplug/spotless)로 실행합니다. 규칙은 [.editorconfig](.editorconfig)에 있습니다.

```bash
./gradlew spotlessCheck   # 검사
./gradlew spotlessApply   # 자동 수정
```

Spotless는 위 명령을 실행할 때만 동작합니다. 커밋 전에 `spotlessApply`를 실행합니다.

### Android Studio 저장 시 자동 정렬

`Settings → Tools → Actions on Save`에서 아래 두 항목을 켭니다.

- **Reformat code**
- **Optimize imports**

Android Studio는 `.editorconfig`를 읽고, ktlint의 `android_studio` 스타일은 Android Studio 기본 Kotlin 스타일에 맞춰져
있어 대부분 같은 결과가 나옵니다. 일부 규칙(예: 와일드카드 import)은 다를 수 있으므로 최종 기준은 `spotlessCheck`입니다.
