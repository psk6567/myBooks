---
name: mybooks-new-module
description: 새 feature/core 모듈을 추가하거나 새 화면 단위를 만들 때 반드시 먼저 읽는다. 모듈 폴더 구조, core/feature 배치 기준, Convention Plugin 사용법, settings 등록 절차를 정한다.
---

# MyBooks 신규 모듈 추가

모듈을 추가할 때 이 구조를 그대로 따른다.

## 모듈 위치

| 종류 | 위치 | 용도 |
|---|---|---|
| feature | `feature/{name}` | 화면 단위 기능 (main, search, favorite, detail ...) |
| core | `core/{name}` | 여러 feature 가 공유하는 것 (common, designsystem, model, domain, data) |

- **배치 기준: 두 개 이상의 feature 가 쓰면 core, 한 feature 만 쓰면 그 feature 안.**
  - 모든 계층이 쓰는 모델 → `core:model` (순수 Kotlin)
  - UseCase → `core:domain` (한 feature 만 쓰더라도)
  - Repository 인터페이스와 구현(internal), API, DB → `core:data`
- 의존 방향은 `feature → core:domain → core:data`, 모두 `core:model` 을 의존한다. (Android 아키텍처 가이드의 UI → Domain → Data)
- feature 는 **다른 feature 를 의존하지 않는다.** 화면 간 이동 연결은 `feature:main` 만 한다.
- feature 는 `core:common`, `core:designsystem`, `core:model`, `core:domain` 을 의존한다. (Convention Plugin 이 자동 추가)
- **feature 는 `core:data` 를 의존하지 않는다.** 데이터는 `core:domain` 의 UseCase 를 거쳐서만 쓴다. (`core:domain` 이 `core:data` 를 `implementation` 으로 의존해 feature 에 노출되지 않는다)

## feature 모듈 구조 (필수)

`{Name}` 은 모듈명의 첫 글자를 대문자로 한 이름 (`search` → `Search`).

```
feature/{name}/
├── build.gradle.kts
└── src/main/kotlin/com/psk/mybooks/feature/{name}/
    ├── ui/
    │   ├── {Name}Screen.kt     # {Name}Route(ViewModel 연결) + {Name}Screen(UI)
    │   ├── {Name}Contract.kt   # State / Intent / (Effect) 정의 (MVI)
    │   ├── {Name}ViewModel.kt  # `MviViewModel` 상속 (internal)
    │   └── {Name}UiState.kt    # UI 모델 (필요할 때)
```

- feature 는 **UI 레이어(Screen, ViewModel, UI 모델)** 만 가진다. UseCase 는 `core:domain`, 데이터는 `core:data` 에 둔다.
- feature 마다 `-data`, `-domain` 모듈을 따로 만들지 않는다.
- `{Name}Route` 와 `{Name}Screen` 은 **같은 파일**(`ui/{Name}Screen.kt`)에 둔다.
  - `Route`: ViewModel 을 만들고 `Screen` 에 상태·콜백을 넘기는 얇은 어댑터
  - `Screen`: 상태와 콜백을 파라미터로만 받는 UI (`internal`). ViewModel 을 직접 참조하지 않는다 → Preview·테스트 가능
- **MVI**: ViewModel 은 `core:common` 의 `MviViewModel<State, Intent, Effect>` 를 상속한다. 상태 변경은 `reduce { copy(...) }` 로만 하고, Screen 은 `state` 와 `onIntent` 만 받는다. Effect 가 없으면 `Nothing`.
  - Route: `viewModel.state` 를 `collectAsStateWithLifecycle()` 로 구독해 `Screen(state, onIntent = viewModel::onIntent)` 에 넘긴다.
  - ViewModel 은 JUnit 만으로 테스트한다. (`{Name}ViewModelTest`)
- 외부에 공개하는 것은 `{Name}Route` 뿐이다. 나머지는 `internal`.
- Navigation 도입 후에는 `navigation/{Name}Navigation.kt`(Route 객체 + `NavGraphBuilder` 확장 함수)가 유일한 공개 진입점이 되고, `{Name}Route` 는 `internal` 로 바꾼다.

### build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.mybooks.android.feature)
}

android {
    namespace = "com.psk.mybooks.feature.{name}"
}
```

그 모듈에서만 쓰는 의존성만 `dependencies { }` 에 추가한다. 공통 설정은 Convention Plugin 이 한다.

## core 모듈

- Compose 를 쓰면 `mybooks.android.library.compose`, 아니면 `mybooks.android.library`.
- Android 에 의존하지 않는 모듈(`core:model`)은 `kotlin.jvm` 플러그인을 쓴다.
- 패키지는 `com.psk.mybooks.core.{name}`.

## 등록 절차

1. 위 구조로 폴더와 파일을 만든다.
2. 루트 `settings.gradle.kts` 에 `include(":feature:{name}")` (또는 `:core:{name}`) 를 추가한다.
3. 화면이 탭/이동 대상이면 `feature:main` 에서 연결한다. (main 만 다른 feature 를 의존)
4. `./gradlew spotlessApply` 로 코드 스타일을 맞춘다.
5. `./gradlew :feature:{name}:assembleDebug` 로 빌드를 확인한다.

## 하지 말 것

- `build-logic` 을 수정하지 않는다. 새 모듈은 기존 Convention Plugin 으로 충분하다. 새로운 "종류"의 모듈이 필요할 때만 사용자에게 먼저 확인한다.
- 모듈마다 `compileSdk`, `minSdk`, Java 버전을 직접 적지 않는다.
- 색상은 `MyBooksTheme.colors.xxx`(시맨틱)만 쓴다. `Color(0x...)`, 토큰(`Red55` 등), `MaterialTheme.colorScheme` 을 화면·컴포넌트에서 직접 쓰지 않는다. 필요한 용도의 색이 없으면 `MbColors` 에 시맨틱 이름을 추가한다.
- **타입 추론에 기대지 않는다.** `val`/`var`(지역 변수·프로퍼티·`by` 위임)과 `=` 로 쓴 함수의 반환 타입은 명시한다. 예: `val colors: MbColors = MyBooksTheme.colors`, `var selected: Int by remember { ... }`. 람다 파라미터와 `for`, 구조 분해는 예외.
- BottomSheet 는 `val sheet: BottomSheetHandle = rememberBottomSheet { dismiss -> ... }` 로 선언하고 `sheet.show()` 로 연다. 화면마다 `if (isShown)` 이나 ViewModel 의 열림 상태(Show/Hide Intent)를 만들지 않는다. ViewModel 은 선택 결과만 Intent 로 받는다.
- 여백은 `Spacer(Modifier.height/width(...))` 대신 `VSpacer(dp)`, `HSpacer(dp)`(`core.designsystem.util`)를 쓴다.
- 아이콘은 디자인 시스템의 `Icon(icon = MbIcon.Xxx, ...)` 만 쓴다. Material `Icon`, `painterResource(R.drawable...)` 를 feature 에서 직접 쓰지 않는다. 새 아이콘은 `MbIcon` 에 추가한다. 슬롯으로 아이콘을 받는 컴포넌트(`TabItem.icon` 등)에도 디자인 시스템 `Icon` 을 넣는다.
- 텍스트 스타일은 `MyBooksTheme.typography.xxx`(normal10~24, bold10~24)만 쓴다. `fontSize`, `fontFamily` 를 직접 지정하지 않는다.
- UI 문구를 코드에 하드코딩하지 않는다. 문자열은 해당 모듈의 `res/values/strings.xml` (+ `values-en`) 에 두고 `stringResource` 로 쓴다.
- 작업이 끝나면 `docs/ai-usage-log.md` 에 기록한다. (프로젝트 CLAUDE.md 규칙)
