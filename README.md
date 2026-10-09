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

## API 키

도서 검색은 [카카오 도서 검색 API](https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-book)를 씁니다.
REST API 키는 저장소에 올리지 않고 프로젝트 루트의 `local.properties`(git 제외)에 넣습니다.

```properties
KAKAO_API_KEY=발급받은_REST_API_키
```

`core:data` 모듈의 `BuildConfig.KAKAO_API_KEY`로 읽습니다. 키가 없으면 빌드는 되지만 API가 401을 돌려줍니다.
BuildConfig에 들어가므로 APK에는 포함됩니다. (과제용이며, 실서비스라면 서버 프록시를 둡니다)

## 모듈 구조

```
app                     Application, MainActivity
build-logic             Convention Plugin (모듈 공통 Gradle 설정)
core
├── common              공통 유틸
├── designsystem        색상·컴포넌트 등 디자인 시스템
├── ui                  여러 화면이 쓰는 UI 공용 코드 (오류를 중요도에 따라 스낵바/다이얼로그로 알리는 ErrorEffect)
├── model               모든 계층이 함께 쓰는 모델 (순수 Kotlin)
├── domain              UseCase
└── data                Repository(인터페이스 + 구현), API, DB
feature
├── main                하단 탭 + 화면 이동 연결
├── search              검색
├── favorite            즐겨찾기
└── detail              도서 상세
```

- **배치 기준: 두 개 이상의 feature가 쓰면 core, 한 feature만 쓰면 그 feature 안에 둡니다.** 단, UseCase는 모두 `core:domain`에 둡니다.
- feature는 core만 의존하고, 다른 feature를 의존하지 않습니다. 화면 간 이동은 `feature:main`이 연결합니다.

#### 계층과 의존 방향

[Android 앱 아키텍처 가이드](https://developer.android.com/topic/architecture)의 계층 구조(UI → Domain → Data)를 따릅니다.

```
feature(UI) ──▶ core:domain(UseCase) ──▶ core:data(Repository, API, DB)
     └──────────────────┴──────────────────────┴──▶ core:model(Book 등)
```

- **Repository는 인터페이스와 구현 모두 `core:data`에 있습니다.** API 응답(DTO)과 DB 엔티티는 `core:data` 안에서 `core:model`로 바꿔서 내보냅니다. UI는 `core:model`만 압니다.
- **feature는 `core:data`를 의존하지 않습니다.** `core:domain`이 `core:data`를 `implementation`으로 의존하므로, feature에서 Repository를 import하면 컴파일 에러가 납니다. 데이터는 UseCase를 거쳐서만 씁니다.
- **모델을 `core:model`로 분리한 이유:** data가 모델을 돌려주고 domain이 data를 의존하므로, 모델을 domain에 두면 서로를 의존하게 됩니다.
- 화면 표시 설정(예: 격자/목록)처럼 도메인이 아닌 값은 그것을 정하는 feature가 가집니다.
- 모듈 공통 설정(SDK 버전, Java 버전, Compose)은 `build-logic`의 Convention Plugin이 담당합니다.

### feature 모듈 내부 구조

feature는 기본적으로 UI 레이어(Screen, ViewModel, UI 모델)만 가집니다. 모듈로 나누는 목적은 레이어 분리가 아니라 화면 단위 격리(빌드 캐시,
`internal` 캡슐화)입니다.

```
feature/{name}/src/main/kotlin/com/psk/mybooks/feature/{name}/
├── ui/
│   ├── {Name}Screen.kt     {Name}Route(ViewModel 연결) + {Name}Screen(UI)
│   └── {Name}ViewModel.kt  상태 관리 (internal)
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
| `mybooks.android.feature`         | 위 설정 + core 모듈 5개(common, designsystem, ui, model, domain), ViewModel 의존성 |

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

### 화면 이동 (Navigation)
한 Activity(`MainActivity`) 안에서 Navigation Compose로 화면을 이동합니다. `NavHost`는 `feature:main`에 있습니다.

```
HomeDestination (검색·즐겨찾기 탭) ──도서 클릭──▶ DetailDestination(book)
```

- feature끼리는 서로를 의존하지 않으므로, 목록 화면은 ViewModel이 `OpenDetail` Effect를 내보내고 Route가 `onBookClick`으로 main에 알리면 main이 `navigateToDetail`을 부릅니다.
- 상세 화면은 `BookUiModel` 전체를 이동 인자(JSON)로 받아서 보여 줄 도서를 저장소에서 다시 찾지 않습니다. 검색 결과는 저장하지 않고 카카오 API에 id 조회가 없기 때문입니다. 상단 바의 즐겨찾기 버튼은 `DetailViewModel`이 저장소를 관찰해 반영하고, 담을 때는 `BookUiModel.toBook()`으로 도메인 모델을 되돌려 저장합니다.
- **앱 스킴**(`mybooks://book/...`)으로 상세를 바로 여는 것은 아직 지원하지 않습니다. 필요한 것은 `DetailNavigation.kt`의 NOTE에 적어 두었습니다.

### 화면용 모델 (UiModel)
화면(State, Screen)은 도메인 모델(`Book`)을 직접 쓰지 않고 `core:ui`의 `BookUiModel`을 씁니다.

- UiModel은 원시 타입(문자열, 숫자)만 가지며, 표시용 가공(저자 합치기, 할인가 판단)은 `Book.toUiModel()` 한 곳에서 합니다.
- ViewModel이 도메인 모델을 읽어서 **내보낼 때** 변환합니다. 하트처럼 도메인 모델 전체가 필요한 동작은 화면이 `id`만 넘기고, ViewModel이 들고 있던 원본으로 처리합니다. (검색 결과는 Paging 변환 시점에 원본을 기억하고 새 검색이면 비웁니다)
- 검색과 즐겨찾기가 같은 목록을 그리므로 UiModel도 하나를 공유합니다.

### 오류 알리기
오류는 화면별 Effect가 아니라 `MviViewModel`의 공통 `error` 흐름으로 보냅니다. 화면은 한 줄로 받아서 중요도에 따라 알립니다.

```kotlin
// ViewModel: 어떤 예외든 보낸다
catch (e: Exception) { sendError(e) }

// Route
ErrorEffect(viewModel.error)
```

- 데이터 계층이 오류를 `ErrorReason`(네트워크, 인증, 권한, 호출 횟수 초과, 서버 등)으로 분류한 `AppException`으로 던지고, 화면은 HTTP 세부 사항을 모릅니다. 분류되지 않은 예외는 `Unknown`입니다.
- **중요도**: 인증·권한 오류는 High(확인 버튼 하나, 닫히지 않는 다이얼로그), 나머지는 Low(스낵바)입니다. 기획이 정해지면 `ErrorReason`의 중요도만 바꾸면 됩니다.
- 검색 결과(Paging)의 불러오기 실패는 화면의 로드 상태로만 알 수 있어서, 화면이 `ReportLoadError` Intent로 ViewModel에 알립니다. 같은 오류는 한 번만 알립니다.

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

### Snackbar 띄우기
스낵바를 그리는 호스트는 `MainScreen`에 한 번만 두고, 어느 화면에서든 손잡이로 한 줄에 띄웁니다.

```kotlin
val snackbar: SnackbarHandle = LocalSnackbar.current
snackbar.show(stringResource(R.string.saved), SnackbarType.Success)   // None, Success, Failed
```

- 종류(`SnackbarType`)에 따라 색과 아이콘이 달라집니다. 동작 버튼이 필요하면 `actionLabel`과 `onAction`을 넘깁니다.
- 새 메시지가 오면 떠 있던 것을 닫고 바로 보여 줍니다. 호스트가 없으면(프리뷰, 테스트) 아무 일도 하지 않습니다.
- 손잡이는 호스트와 같은 수명이라 호출한 화면이 사라져도 스낵바가 끊기지 않습니다.

### Dialog 띄우기
화면을 막고 확인이나 선택을 받는 `MbDialog`는 버튼 구성을 `DialogButtons`로 고릅니다. 열려 있는지는 호출한 쪽이 정하고, 그릴 때만 보입니다.

```kotlin
if (showDialog) {
    MbDialog(
        title = "즐겨찾기 삭제",
        message = "이 도서를 즐겨찾기에서 삭제할까요?",
        buttons = DialogButtons.Two(confirmText = "삭제", dismissText = "취소", onConfirm = { ... }, onDismiss = { ... }),
        onDismissRequest = { showDialog = false }
    )
}
```

- 버튼 하나는 `DialogButtons.One(confirmText, onConfirm)`, 두 개는 `DialogButtons.Two(...)`입니다. 제목(`title`)은 생략할 수 있습니다.
- 버튼을 눌러도 닫히지 않으므로, 닫는 것은 `onConfirm`/`onDismiss` 안에서 호출한 쪽이 합니다.
- `cancelable = false`면 바깥을 누르거나 뒤로가기를 눌러도 닫히지 않고 버튼으로만 닫힙니다. 사용자가 반드시 확인해야 하는 오류에 씁니다.

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
