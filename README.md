# MyBooks

카카오 도서 검색 API로 도서를 찾고 즐겨찾기를 로컬에 저장하는 Android 앱입니다. Kotlin, Coroutine/Flow, Jetpack Compose로 만들었고,
"여러 기능이 계속 추가되고 여러 개발자가 함께 작업하는 대규모 앱의 한 기능"이라는 전제로 구조를 잡았습니다.

- 어떻게 빌드하나 → [빌드 방법](#빌드-방법)
- 무엇을 왜 그렇게 만들었나 → [주요 구현 포인트와 판단 근거](#주요-구현-포인트와-판단-근거)
- 고려했지만 하지 않은 것 → [고려했지만 구현하지 않은 것](#고려했지만-구현하지-않은-것)
- 불안정한 네트워크·프로세스 종료를 직접 재현해 보려면 → [개발자 모드](#개발자-모드-debug-빌드-전용)
- AI를 어떻게 썼나 → [AI 활용](#ai-활용)

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

## 빌드 방법

1. JDK 17과 Android Studio 최신 안정 버전을 준비합니다. (Gradle Wrapper 포함, 별도 설치 불필요)
2. 프로젝트 루트의 `local.properties`에 카카오 REST API 키를 넣습니다. ([API 키](#api-키) 참고, 제출물에는 키가 든 파일이 포함되어 있습니다)
3. 빌드합니다.

```bash
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease      # 난독화·축소(R8) 적용, debug 키로 서명 (app/build/outputs/apk/release, mapping은 outputs/mapping/release)
./gradlew testDebugUnitTest    # 단위 테스트
./gradlew spotlessCheck        # 코드 스타일 검사
```

## 개발자 모드 (debug 빌드 전용)

재현하기 어려운 상황(느린 네트워크, 프로세스 종료 등)을 직접 만들어 보는 도구입니다. **debug 빌드에서 메인 화면의 제목("검색", "즐겨찾기")을 3초 안에
7번 누르면** `DevActivity`가 열립니다.

| 기능              | 하는 일                                                                                                                                    |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| 네트워크 테스트        | 도서 검색 API 요청만 느리게(8초) 하거나 시간 초과, 서버 오류(500), 인증 실패(401), 연결 실패, "처음 2번만 실패 후 성공"으로 만듭니다. 검색어를 바꾸거나 "다시 시도"를 눌러 스켈레톤, 재시도, 오류 안내를 확인합니다. |
| Warm start      | 프로세스는 두고 메인 화면(Activity)만 다시 만듭니다. (회전과 같은 경로)                                                                                          |
| LMK             | 앱을 백그라운드로 보내고 1.5초 뒤 프로세스를 종료합니다. 최근 앱 목록에서 다시 열어 탭·검색어·정렬이 복원되는지 확인합니다.                                                                |
| 디자인 시스템 Preview | 디자인 시스템의 `@Preview`를 실행 중에 찾아 목록으로 보여 주고, 누르면 기기에서 전체 화면으로 엽니다.                                                                         |

- release 빌드에는 들어가지 않습니다. `debug`/`release` 소스셋을 나누어(`DevEntry`, `DevInterceptors`는 release에 빈 구현),
  release APK에는 관련 클래스가
  없는 것을 확인했습니다.
- 네트워크 시뮬레이션은 검색 API 클라이언트의 가장 안쪽 인터셉터(`DevFaultInterceptor`)로 넣어서, 가짜 오류도 실제 서버 오류처럼 오류 변환과 자동
  재시도를 거칩니다. 이미지 요청에는 영향이 없고, 앱을 다시 시작하면 초기화됩니다.
- Preview 목록은 `...Kt` 클래스에서 이름에 `Preview`가 들어간 Composable을 찾아 만들기 때문에 목록을 따로 관리하지 않습니다.

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
| 화면 이동      | Navigation Compose                           | 2.10.2         |
| 페이징        | Paging 3 (+ paging-compose)                  | 3.5.2          |
| 이미지        | Coil 3 (OkHttp 연동)                           | 3.6.3          |

버전은 모두 [gradle/libs.versions.toml](gradle/libs.versions.toml)에서 관리합니다.

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

- **배치 기준: 두 개 이상의 feature가 쓰면 core, 한 feature만 쓰면 그 feature 안에 둡니다.** 단, UseCase는 모두 `core:domain`에
  둡니다.
- feature는 core만 의존하고, 다른 feature를 의존하지 않습니다. 화면 간 이동은 `feature:main`이 연결합니다.

#### 계층과 의존 방향

[Android 앱 아키텍처 가이드](https://developer.android.com/topic/architecture)의 계층 구조(UI → Domain → Data)를
따릅니다.

```
feature(UI) ──▶ core:domain(UseCase) ──▶ core:data(Repository, API, DB)
     └──────────────────┴──────────────────────┴──▶ core:model(Book 등)
```

- **Repository는 인터페이스와 구현 모두 `core:data`에 있습니다.** API 응답(DTO)과 DB 엔티티는 `core:data` 안에서 `core:model`로
  바꿔서 내보냅니다. UI는 `core:model`만 압니다.
- **feature는 `core:data`를 의존하지 않습니다.** `core:domain`이 `core:data`를 `implementation`으로 의존하므로,
  feature에서 Repository를 import하면 컴파일 에러가 납니다. 데이터는 UseCase를 거쳐서만 씁니다.
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

| 플러그인                              | 용도                                                                        |
|-----------------------------------|---------------------------------------------------------------------------|
| `mybooks.android.library`         | Android 라이브러리 모듈 공통 설정                                                    |
| `mybooks.android.library.compose` | 위 설정 + Compose                                                            |
| `mybooks.android.feature`         | 위 설정 + core 모듈 5개(common, designsystem, ui, model, domain), ViewModel 의존성 |

새 모듈은 기존 플러그인으로 충분하며, 새로운 종류의 모듈이 필요할 때만 `build-logic`에 추가합니다.

## 아키텍처 패턴: MVI

모든 feature의 ViewModel은 `core:common`의 `MviViewModel<State, Intent, Effect>`를 상속합니다.

```
화면 ──Intent──▶ ViewModel ──reduce──▶ State ──▶ 화면
                    └──────Effect(일회성)──────▶ 화면
```

| 구성         | 역할                         | 규칙                                      |
|------------|----------------------------|-----------------------------------------|
| **State**  | 화면이 그리는 모든 상태              | 불변 `data class`, 화면은 `state`만 보고 그린다    |
| **Intent** | 사용자 행동 등 화면 → ViewModel 입력 | `sealed interface`, `onIntent()`로만 들어온다 |
| **Effect** | 한 번만 처리할 일(토스트, 이동 등)      | 없으면 `Nothing`                           |

- 상태 변경은 `reduce { copy(...) }`로만 합니다.
- State/Intent/Effect는 화면 파일 옆의 `{Name}Contract.kt`에 둡니다.
- `Screen`은 `state`와 `onIntent`만 받습니다. ViewModel을 직접 모르므로 Preview와 테스트가 쉽습니다.
- ViewModel은 Android 의존 없이 JUnit만으로 테스트합니다. (예: `MainViewModelTest`)

### 화면 이동 (Navigation)

한 Activity(`MainActivity`) 안에서 Navigation Compose로 화면을 이동합니다. `NavHost`는 `feature:main`에 있습니다.

```
HomeDestination (검색·즐겨찾기 탭) ──도서 클릭──▶ DetailDestination(book)
```

- feature끼리는 서로를 의존하지 않으므로, 목록 화면은 ViewModel이 `OpenDetail` Effect를 내보내고 Route가 `onBookClick`으로 main에
  알리면 main이 `navigateToDetail`을 부릅니다.
- 상세 화면은 `BookUiModel` 전체를 이동 인자(JSON)로 받아서 보여 줄 도서를 저장소에서 다시 찾지 않습니다. 검색 결과는 저장하지 않고 카카오 API에 id
  조회가 없기 때문입니다. 상단 바의 즐겨찾기 버튼은 `DetailViewModel`이 저장소를 관찰해 반영하고, 담을 때는 `BookUiModel.toBook()`으로 도메인
  모델을 되돌려 저장합니다.
- **앱 스킴**(`mybooks://book/...`)으로 상세를 바로 여는 것은 아직 지원하지 않습니다. 이미 떠 있는 앱이 받은 새 Intent를
  `navController.handleDeepLink`로 넘기는 것까지만 `MainRoute`에 준비해 두었고(등록된 딥링크가 없어 지금은 아무 일도 하지 않음), 나머지는
  `DetailNavigation.kt`의 NOTE/TODO에 적어 두었습니다.

### 넓은 화면 (태블릿, 폴더블)

`isWideScreen()`(앱 창 폭 640dp 이상)이 true 면 홈이 아래 구성으로 바뀝니다. 좁은 화면(폰)은 기존 그대로입니다.

|         | 좁은 화면              | 넓은 화면                          |
|---------|--------------------|--------------------------------|
| 탭       | 하단 `TabBar`        | 왼쪽 `TabRail`                   |
| 격자      | 폭에 맞춰 열 수 자동(폰 3열) | 목록 영역(전체 폭의 약 43%)에 맞춰 같은 방식으로 자동 |
| 도서를 누르면 | 상세 화면으로 이동         | 이동 없이 오른쪽 패널(`DetailPane`)에 상세 |

- 기기 화면이 아니라 **앱 창** 폭을 보므로, 폴더블을 펼치거나 분할 화면으로 폭이 바뀌면 구성이 바로 바뀝니다.
- 격자의 열 수(`BookGridColumns`, 항목 최소 폭 110dp)는 두 구성 모두 같은 규칙이고, 넓은 화면에서는 상세 패널이 폭을 나누어 가져서 목록 영역이 좁아집니다.
  그래서 열 수가 화면 폭만큼 늘지는 않고 폰과 비슷하거나 더 적을 수 있습니다. (예: 펼친 폴더블의 목록 영역은 2열)
- 상세는 `DetailViewModel`이 도서를 직접 받아서(도서마다 ViewModel) 화면 이동과 패널이 같은 화면을 씁니다.
- 선택한 도서(`selectedBook`)는 넓은 화면에서만 쓰입니다. 좁은 화면으로 돌아가도 상태는 남지만 화면에는 영향이 없습니다.

### 화면용 모델 (UiModel)

화면(State, Screen)은 도메인 모델(`Book`)을 직접 쓰지 않고 `core:ui`의 `BookUiModel`을 씁니다.

- UiModel은 원시 타입(문자열, 숫자)만 가지며, 표시용 가공(저자 합치기, 할인가 판단)은 `Book.toUiModel()` 한 곳에서 합니다.
- ViewModel이 도메인 모델을 읽어서 **내보낼 때** 변환합니다. 하트처럼 도메인 모델 전체가 필요한 동작은 화면이 `id`만 넘기고, ViewModel이 들고 있던
  원본으로 처리합니다. (검색 결과는 Paging 변환 시점에 원본을 기억하고 새 검색이면 비웁니다)
- 검색과 즐겨찾기가 같은 목록을 그리므로 UiModel도 하나를 공유합니다.

### 오류 알리기

오류는 화면별 Effect가 아니라 `MviViewModel`의 공통 `error` 흐름으로 보냅니다. 화면은 한 줄로 받아서 중요도에 따라 알립니다.

```kotlin
// ViewModel: 어떤 예외든 보낸다
catch(e: Exception) { sendError(e) }

// Route
ErrorEffect(viewModel.error)
```

- 데이터 계층이 오류를 `ErrorReason`(네트워크, 인증, 권한, 호출 횟수 초과, 서버 등)으로 분류한 `AppException`으로 던지고, 화면은 HTTP 세부
  사항을 모릅니다. 분류되지 않은 예외는 `Unknown`입니다.
- **중요도**: 인증·권한 오류는 High(확인 버튼 하나, 닫히지 않는 다이얼로그), 나머지는 Low(스낵바)입니다. 기획이 정해지면 `ErrorReason`의 중요도만
  바꾸면 됩니다.
- 검색 결과(Paging)의 불러오기 실패는 목록 안에서 알립니다. 첫 로드 실패는 화면 가운데 문구와 "다시 시도" 버튼, 더 불러오기 실패는 목록 끝의 안내와 버튼입니다(
  `ListErrorView`, `ListFooterError`). 스낵바는 몇 초 뒤 사라지므로 재시도 버튼을 두지 않습니다.
- 화면은 `ReportLoadError` Intent로 오류를 ViewModel에 알리지만, 다시 시도할 수 있는 오류는 목록이 이미 보여 주므로 무시하고 High(인증·권한)만
  다이얼로그로 보냅니다.

### main 화면의 탭 이동

`MainViewModel`이 선택된 탭을 상태로 가지고, 탭 클릭과 Pager 스와이프가 모두 `SelectTab` Intent로 들어옵니다.
상태(선택된 탭)가 Pager 이동의 기준이고, Pager가 넘어가면 다시 Intent로 상태를 맞춥니다.

화면 조건(탭, 검색어, 검색·즐겨찾기 정렬, 금액 필터, 보기 방식, 선택한 도서)은 `SavedStateHandle`에 JSON 한 줄로 계속 저장합니다. 메모리 부족 등으로
프로세스가 종료됐다가 돌아와도 이전 조건으로 이어지고, 이때는 앱을 새로 연 것이 아니므로 오프라인 이동은 하지 않습니다. 검색·즐겨찾기 화면의 조건은 이
값을 받아 쓰기만 하므로 따로 저장하지 않고, 읽을 수 없는 옛 값은 무시하고 기본 상태로 시작합니다.

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
        buttons = DialogButtons.Two(
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = { ... },
            onDismiss = { ... }),
        onDismissRequest = { showDialog = false }
    )
}
```

- 버튼 하나는 `DialogButtons.One(confirmText, onConfirm)`, 두 개는 `DialogButtons.Two(...)`입니다. 제목(`title`)은
  생략할 수 있습니다.
- 버튼을 눌러도 닫히지 않으므로, 닫는 것은 `onConfirm`/`onDismiss` 안에서 호출한 쪽이 합니다.
- `cancelable = false`면 바깥을 누르거나 뒤로가기를 눌러도 닫히지 않고 버튼으로만 닫힙니다. 사용자가 반드시 확인해야 하는 오류에 씁니다.
- 상자(제목, 메시지, 버튼)는 창(`Dialog`)과 분리된 `DialogContent`로 두었고, Preview는 이것만 그립니다. 창은 화면 전체와 뒤로가기를 가져가
  Preview
  도구나 개발자 모드에서 다루기 어렵기 때문입니다.

## 디자인 시스템 - 색상

색상은 두 단계로 나눕니다.

| 단계                  | 파일               | 이름 예시                               | 공개 범위                        |
|---------------------|------------------|-------------------------------------|------------------------------|
| Color Token (팔레트)   | `ColorTokens.kt` | `Red55`, `Gray92` (계열 + 톤)          | `internal` (designsystem 내부) |
| Semantic Color (용도) | `MbColors.kt`    | `textPrimary`, `favorite`, `border` | public                       |

- 화면과 컴포넌트는 **`MyBooksTheme.colors.xxx`만** 사용합니다. 토큰과 `MaterialTheme.colorScheme`은 직접 쓰지 않습니다.
- 토큰은 `internal`이라 feature 모듈에서는 쓰는 순간 컴파일 에러가 납니다.
- Material 컴포넌트가 같은 톤을 내도록 `Theme.kt`에서 Material `ColorScheme`에 매핑합니다.
- 브랜드 컬러 `#F83949`
  를 [Material Color Utilities](https://github.com/material-foundation/material-color-utilities)에 넣어
  팔레트를 생성했습니다. 배경과 글자는 무채색(Gray)을 씁니다.
- 라이트/다크 모드를 지원합니다. 다이내믹 컬러는 쓰지 않습니다.

## 디자인 시스템 - 타이포그래피

- 폰트: [여기어때 잘난체 2](https://www.gccompany.co.kr) (개인·기업 무료, 수정·재배포 허용) —
  `core/designsystem/src/main/res/font/jalnan2.ttf`
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

주요 규칙은 `.editorconfig`에 있습니다.

### Android Studio 저장 시 자동 정렬

`Settings → Tools → Actions on Save`에서 아래 두 항목을 켭니다.

- **Reformat code**
- **Optimize imports**

Android Studio는 `.editorconfig`를 읽고, ktlint의 `android_studio` 스타일은 Android Studio 기본 Kotlin 스타일에 맞춰져
있어 대부분 같은 결과가 나옵니다. 일부 규칙(예: 와일드카드 import)은 다를 수 있으므로 최종 기준은 `spotlessCheck`입니다.

## 주요 구현 포인트와 판단 근거

### 구조

- 멀티 모듈 + 클린 아키텍처(`feature → core:domain → core:data`, 모두 `core:model` 공유). 한 기능에 사람이 더 붙어도 모듈 경계가
  충돌 범위를 줄입니다.
  feature는 데이터 계층(`core:data`)을 볼 수 없고 UseCase만 씁니다. (`implementation` 의존이라 컴파일 단계에서 막힘)
- MVI(`MviViewModel<State, Intent, Effect>`). 화면은 `state`와 `onIntent`만 받아서 Preview·테스트가 쉽고, 오류는 모든
  화면이 같은 `error` 흐름으로 알립니다.
- 디자인 시스템(토큰 색 + 시맨틱 색, 타이포, 컴포넌트)과 문구 리소스(ko 기본 + en)를 분리해 화면 코드에 색·글자·아이콘을 직접 쓰지 않습니다.
- 규칙은 사람이 기억하지 않아도 되도록 도구로 강제합니다: Convention Plugin(모듈 공통 설정), Spotless/ktlint, 신규 모듈 Skill.

### 기능

- 검색은 Paging 3(20개 단위, 정확도/발간일 정렬), 입력은 디바운스. 카카오 API 응답의 중복은 `BookPagingSource`에서 걸러냅니다.
- 즐겨찾기는 Room에 도서 정보 전체를 저장해 오프라인에서도 목록·상세가 열립니다. 즐겨찾기 탭의 검색·제목 정렬·금액(범위 슬라이더) 필터는 로컬에서 처리합니다.
- 상세 화면은 목록의 도서를 이동 인자로 받습니다. 카카오 API에 id 조회가 없어서 다시 찾을 수 없기 때문입니다.
- 검색 결과에는 즐겨찾기 여부를 합쳐서 보여 주므로 하트가 탭 사이에서 어긋나지 않습니다.
- 프로세스가 종료됐다가 돌아와도 화면 조건이 이어집니다. ([main 화면의 탭 이동](#main-화면의-탭-이동) 참고)
- 목록 카드에 출간일을 보여 주고, 넓은 화면에서는 옆 패널에 열린 도서를 목록에서 테두리로 강조합니다.
- release 빌드는 R8 코드 난독화·축소와 리소스 축소를 적용합니다. 직렬화·Retrofit·Room·Hilt는 라이브러리의 기본 규칙으로 충분해 직접 keep 규칙을 두지
  않았고, 릴리스 APK로 검색·상세·즐겨찾기를 실행해 확인했습니다. 오류 로그의 줄 번호를 되살릴 수 있도록 `mapping.txt`와 줄 번호 정보를 유지합니다.

### 네트워크가 불안정한 환경

| 상황              | 대응                                                                                                                                          |
|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| 응답이 오지 않음       | 연결 5초·읽기/쓰기 10초 타임아웃 (`NetworkModule`)                                                                                                      |
| 일시적 실패          | 검색 API 호출만 최대 3번 시도(0.5초·1초 간격). 서버 5xx·연결 오류·시간 초과만 재시도하고, 인증·권한·잘못된 요청·429는 쿼터만 쓰므로 재시도하지 않음 (`BookApiRetryInterceptor`)                  |
| 오류의 종류          | `ErrorReason`(네트워크, 시간 초과, 인증, 권한, 호출 횟수 초과, 서버, 알 수 없음)에 중요도와 재시도 가능 여부를 둠                                                                 |
| 오류 알림 방식        | 목록 안에서 알림: 첫 로드 실패는 화면 가운데 문구 + "다시 시도", 더 불러오기 실패는 목록 끝 안내 + 버튼. 스낵바는 몇 초 뒤 사라져 누를 기회가 없으므로 재시도 버튼을 두지 않고, 인증·권한처럼 사용자가 해결할 수 없는 오류만 다이얼로그 |
| 첫 로딩            | 항목 모양의 스켈레톤(목록·격자). 이전 목록이 남아 있는 재검색은 그대로 유지                                                                                                |
| 앱을 열었을 때 인터넷 없음 | 즐겨찾기가 없어도 즐겨찾기 탭에서 시작하고 안내 스낵바 (`IsOnlineUseCase`)                                                                                          |

- 이 대응은 [개발자 모드](#개발자-모드-debug-빌드-전용)의 네트워크 테스트로 직접 재현해 볼 수 있습니다.
- API 키는 카카오 클라이언트에만 붙입니다. 이미지(Coil)도 쓰는 공용 OkHttp 클라이언트에는 키가 없습니다.
- 서버 오류 `message`는 예외에 넣지 않습니다. (401 응답이 키를 되돌려 보내는 경우가 있어 로그·화면에 새면 안 됨)

### 태블릿·폴더블

- 기기가 아니라 **앱 창 폭**(640dp)으로 판단해서 폴더블을 펼치거나 분할 화면으로 폭이 바뀌면 구성이 바로 바뀝니다. ([넓은 화면](#넓은-화면-태블릿-폴더블) 참고)
- 하단 탭 → 왼쪽 탭 레일, 목록 옆에 상세 패널(목록-상세). 격자의 열 수는 항목 최소 폭(110dp)으로 정하는 자동 방식이라 화면이 아니라 목록 영역의 폭을 따릅니다.

## 고려했지만 구현하지 않은 것

| 항목                                | 이유 / 다음 단계                                                                                                                                                                  |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 검색 결과 오프라인 캐시(HTTP 캐시 또는 Room)    | 무엇을 몇 개까지 얼마나 오래 보관할지 기획이 필요하고, 즐겨찾기가 이미 오프라인에서 열려서 범위 밖으로 둠. Paging `RemoteMediator`로 확장 가능                                                                                |
| 앱 사용 중 연결 끊김/복구 감지(자동 새로고침)       | 앱을 열 때만 확인(`IsOnlineUseCase`). 사용 중에는 목록 안 오류와 "다시 시도"가 맡음. `ConnectivityManager` 콜백을 Flow로 바꾸면 됨                                                                           |
| 연결 시간 초과를 "네트워크 없음"으로 분류          | 지금은 연결·읽기 시간 초과 모두 "서버 응답이 늦어요"로 안내. 구분은 기획 확인 후 `toAppException()`에서 한 줄                                                                                                   |
| 앱 스킴(`mybooks://book/...`) 상세 딥링크 | 새 Intent를 `handleDeepLink`로 넘기는 것까지만 준비(`MainRoute`). 나머지는 `DetailNavigation.kt`의 TODO. 도서 id 조회 API가 없어 ISBN 검색이 필요하고, 카카오 `target=isbn` 검색은 없는 ISBN에도 비슷한 도서를 돌려주는 점을 확인함 |
| 검색 목록 스크롤 위치·불러온 페이지 복원           | 프로세스 종료 후에는 조건만 복원하고 목록은 첫 페이지부터 다시 불러옴. 위치와 페이지 키까지 저장하면 가능(Paging `getRefreshKey`)                                                                                        |
| 금액을 숫자로 직접 입력하는 필터                | 슬라이더(1,000원 단위)로 대체. `AmountField`는 남아 있지만 쓰이지 않음                                                                                                                           |
| 화면 UI(Compose) 테스트                | ViewModel·Paging·Interceptor·분류 로직의 단위 테스트 중심. UI는 Preview와 에뮬레이터 확인으로 대체                                                                                                   |

## AI 활용

AI 도구는 Claude Code(CLI)를 썼습니다. 작업 단위마다 [docs/ai-usage-log.md](docs/ai-usage-log.md)에 **활용한 작업 / 검증
방법 / 직접 판단한 부분 / AI의 실수와 정정**을 남겼습니다.
AI에게 주는 규칙은 [CLAUDE.md](CLAUDE.md)와 [.claude/skills](.claude/skills/mybooks-new-module/SKILL.md)에
있습니다.

### 어떤 작업에 활용했나

- 모듈 구조·Convention Plugin·Hilt·Room·Retrofit·Paging 등 뼈대 구성과 반복 코드 작성
- 디자인 시스템 컴포넌트(스낵바, 다이얼로그, BottomSheet, 슬라이더, 스켈레톤 등)와 화면 구현
- 요구사항 분석과 계획(불안정한 네트워크, 태블릿·폴더블)에서 선택지와 장단점을 내게 하고 비교
- 단위 테스트 작성, 에뮬레이터 조작을 통한 동작 확인, 문서(README, 작업 기록) 정리

### 어떻게 검증했나

- 로직은 **단위 테스트**(ViewModel, Paging 소스, 재시도 인터셉터, 오류 분류, 필터·정렬)로 확인하고, 매 작업마다 `spotlessCheck`와 앱 빌드를
  돌렸습니다.
- 화면은 에뮬레이터(폰, 폴더블)에서 직접 확인했습니다. 네트워크를 끊거나 느리게 만들어 오류 UI, 재시도, 스켈레톤을 보았고, `uiautomator`와 `logcat`으로
  글자·크래시·중복 알림을 확인했습니다.
- 불안정한 네트워크와 프로세스 종료는 [개발자 모드](#개발자-모드-debug-빌드-전용)로 만들어 확인했습니다. (인증 실패 다이얼로그, 재시도, Warm start,
  LMK 후 상태 복원)
- **확인하지 못한 것은 작업 기록에 따로 적었습니다.** (예: 실제 느린 응답에서의 타임아웃 흐름, 일부 넓은 화면 조합)

### 그대로 쓰지 않고 직접 판단해 바꾼 부분

AI가 낸 결과를 그대로 받지 않고, 구조와 방향은 사람이 정하고 AI가 구현하는 방식으로 진행했습니다.

- **아키텍처 설계를 주도**: 클린 아키텍처, 멀티 모듈, 디자인 시스템, 다국어라는 요구를 사람이 정했습니다. AI가 한 번에 만든 core 8개 모듈 구조를 전부 버리고
  단계별로 다시 쌓았고, feature를 화면 기준(search / favorite / detail)으로 나눈 뒤 하단 탭을 위해 `feature:main`을 두었습니다.
  "feature끼리 의존하지 않고 이동은 main이 연결"하는 규칙은 AI가 보충했습니다. 이후에도 불필요한 빈 계층·파일을 짚어 줄였습니다.
- **디자인 시스템의 경계**: 색을 토큰과 시맨틱으로 나눠 컴포넌트가 토큰 색을 직접 쓰지 못하게 했고, 아이콘도 리소스 id 대신 `MbIcon` enum으로만 받게 해
  feature가 임의의 drawable을 넘기지 못하도록 컴파일러가 막게 했습니다. UI 헬퍼(`VSpacer` 등)는 `core:common`이 아니라 디자인 시스템에 두도록
  바로잡았습니다.
- **패턴과 화면 구조 결정**: MVI 적용, 탭 이동 상태를 ViewModel에 두기, Scaffold는 Activity가 아니라 `feature:main`에 두기 등을
  제안·결정했습니다.
- **개발 환경 검증**: 요구 환경이 유지되는지 재점검을 요청해, 템플릿이 Gradle 데몬을 JDK 21로 고정해 JDK 17 조건을 어기고 있던 것을 찾아 고쳤습니다.
  (AI도 이 문제를 잘못된 방향으로 덮어 두었었음)
- **요구사항 해석과 제품 판단**: 불안정한 네트워크 대응의 범위와 순서를 사람이 정했습니다. 재시도는 몇 초 뒤 사라지는 스낵바가 아니라 목록 안에 두기로 했고,
  오프라인으로 열면 즐겨찾기가 없어도 즐겨찾기 탭으로 옮기기로 했습니다.
- **코드 스타일 규칙**: 타입 명시, 체이닝 줄바꿈 등을 사람이 정하고 AI가 전체에 적용했습니다.

그 밖의 판단과, AI의 실수와 정정(예: 확인 없이 되돌려 수정이 사라진 일, 검증 환경 문제로 잘못 본 결과) 등 자세한 내용은
[docs/ai-usage-log.md](docs/ai-usage-log.md)에서 확인할 수 있습니다.
