<p align="center">
  <img src="images/icon.png" alt="Compose Input Actions Logo" width="150" />
</p>

# Compose Input Actions

[English](README.md) | **한국어**

[![Kotlin](https://img.shields.io/badge/kotlin-2.4.0-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-blue)](https://github.com/JetBrains/compose-multiplatform)
[![Platform](https://img.shields.io/badge/platform-android%20%7C%20ios%20%7C%20desktop%20%7C%20macos-lightgrey.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Compose Multiplatform의 `TextField` / `BasicTextField`에 플랫폼 네이티브 텍스트 입력 액션 및 키보드 악세서리 툴바를 제공하는 Kotlin Multiplatform (KMP) 라이브러리입니다.

iOS 환경에서는 Compose가 소유한 텍스트 입력 상태와 포커스 라이프사이클을 그대로 유지하면서, 키보드 상단에 네이티브 `UIToolbar` 액세서리 뷰를 동적으로 연결합니다. Android, Desktop (JVM), macOS 환경에서는 기존 입력 및 IME 동작을 방해하지 않고 안전하게 통과(no-op) 처리하여 공통 Compose 코드베이스 전체에서 완벽한 멀티플랫폼 호환성을 보장합니다.

---

## 스크린샷

<p align="center">
  <img src="images/sample_01_ko.png" width="30%" alt="샘플 1" />
  <img src="images/sample_02_ko.png" width="30%" alt="샘플 2" />
  <img src="images/sample_03_ko.png" width="30%" alt="샘플 3" />
</p>
<p align="center">
  <img src="images/sample_04_ko.png" width="30%" alt="샘플 4" />
  <img src="images/sample_05_ko.png" width="30%" alt="샘플 5" />
  <img src="images/sample_06_ko.png" width="30%" alt="샘플 6" />
</p>

## 주요 기능

- **Compose 관용적 Modifier API**: `Modifier.inputActions(...)`를 통해 Compose `TextField`에 손쉽게 네이티브 액션을 부착합니다.
- **포커스 연동 라이프사이클**: 텍스트 필드의 포커스 획득 및 상실 시 자동으로 툴바를 표시하거나 해제합니다.
- **SF Symbols 및 커스텀 항목**: SF Symbol 기반 네이티브 아이콘, Plain/Done 스타일, 가변 간격 요소(`InputAction.FlexibleSpace`)를 지원합니다.
- **크로스 플랫폼 타깃 지원**: **iOS**, **Android**, **Desktop (JVM)**, **macOS** 타깃을 공식 지원합니다. `commonMain` 영역에서 `InputActionsHost`로 UI 트리를 감싸 손쉽게 멀티플랫폼에 적용할 수 있습니다.

### 지원 기능 및 플랫폼 매트릭스

| 기능 | iOS | Android | Desktop (JVM) | macOS | 완성도 | 구현 방식 |
| :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| **InputActionsHost** | 🟢 지원 | 🟢 지원 | 🟢 지원 | 🟢 지원 | **100%** | 활성 액션 레지스트리를 공급하는 공통 CompositionLocal 호스트 |
| **Modifier.inputActions** | 🟢 지원 | 🟢 지원 | 🟢 지원 | 🟢 지원 | **100%** | `FocusEventModifierNode` 이벤트를 감지하는 Modifier Node |
| **Plain & Done 액션** | 🟢 지원 | 🟡 No-op | 🟡 No-op | 🟡 No-op | **95%** | iOS `Plain` / `Done` 스타일의 네이티브 `UIBarButtonItem` |
| **아이콘 액션 (SF Symbols)** | 🟢 지원 | 🟡 No-op | 🟡 No-op | 🟡 No-op | **95%** | iOS `UIImage.systemImageNamed(...)` 연동 |
| **Flexible Spacers** | 🟢 지원 | 🟡 No-op | 🟡 No-op | 🟡 No-op | **100%** | iOS `UIBarButtonItem(barButtonSystemItem: .flexibleSpace)` |
| **동적 액션 업데이트** | 🟢 지원 | 🟢 지원 | 🟢 지원 | 🟢 지원 | **90%** | 포커스 유지 중 액션 목록 변경 시 툴바 아이템 실시간 갱신 |

---

## 설치 방법

공유 Kotlin Multiplatform 모듈의 `build.gradle.kts` 파일에 의존성을 추가합니다:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("zone.ien.inputactions:inputactions:0.0.0")
        }
    }
}
```

### 설정 방법

공유 모듈(`commonMain`)의 Compose UI 트리 상위에 `InputActionsHost`를 감싸줍니다:

```kotlin
// commonMain/kotlin/App.kt
import zone.ien.inputactions.InputActionsHost

@Composable
fun App() {
    MaterialTheme {
        InputActionsHost {
            MainScreen()
        }
    }
}
```

- **iOS**: `InputActionsHost`가 `Modifier.inputActions(...)`가 적용된 필드의 포커스 이벤트를 감지하여 iOS 활성 키보드 Responder에 네이티브 `UIToolbar`를 부착합니다.
- **Android**: `InputActionsHost`가 공통 컨테이너(no-op)로 동작하여 별도 레이아웃 변경 없이 기존 Android 소프트웨어 키보드 IME 동작이 유지됩니다.

> [!IMPORTANT]
> **툴체인 요구사항**: 본 라이브러리는 **Kotlin 2.4.0** 및 **Compose Multiplatform 1.11.1** (Compose 1.12.x 계열 호환)을 기준으로 작성 및 검증되었습니다.

---

## API 명세 및 파라미터 설명

### 1. `InputActionsHost`

CompositionLocal을 통해 로컬 입력 액션 레지스트리 호스트를 구성하는 `@Composable` 래퍼 컴포넌트입니다.

| 파라미터 | 타입 | 기본값 | 설명 |
| :--- | :--- | :--- | :--- |
| `content` | `@Composable () -> Unit` | **필수** | `inputActions`가 설정된 `TextField`들을 포함하는 하위 UI 트리. |

---

### 2. `Modifier.inputActions`

텍스트 필드가 포커스를 획득했을 때 활성화될 네이티브 액션들을 등록하는 Modifier 확장 함수입니다.

| 파라미터 | 타입 | 기본값 | 설명 |
| :--- | :--- | :--- | :--- |
| `vararg actions` | `InputAction` | **필수** | 키보드 툴바에 표시할 `InputAction` 가변 인자 목록. |

---

### 3. `InputAction`

키보드 악세서리 툴바에 표시될 액션 버튼 또는 가변 간격 요소를 정의하는 클래스입니다.

```kotlin
class InputAction(
    val title: String = "",
    val style: InputActionStyle = InputActionStyle.Plain,
    val icon: InputActionIcon? = null,
    val hidesSharedBackground: Boolean = false,
    val separatesSharedBackground: Boolean = false,
    val onClick: () -> Unit,
)
```

| 프로퍼티 | 타입 | 기본값 | 설명 |
| :--- | :--- | :--- | :--- |
| `title` | `String` | `""` | 액션 버튼에 표시될 텍스트 라벨입니다. 아이콘 전용 버튼인 경우 빈 값일 수 있으며, `FlexibleSpace`에서는 무시됩니다. |
| `style` | `InputActionStyle` | `InputActionStyle.Plain` | 버튼 표시 스타일 (`Plain`, `Done`). |
| `isFlexibleSpace` | `Boolean` | `false` | 해당 항목이 가변 스페이서인지(`true`), 일반 액션 버튼인지(`false`) 여부를 나타냅니다. |
| `icon` | `InputActionIcon?` | `null` | 옵셔널 네이티브 아이콘. iOS 환경에서 SF Symbol 아이콘을 표시합니다. |
| `hidesSharedBackground` | `Boolean` | `false` | iOS 26+ 이상에서 해당 버튼/그룹의 공유 툴바 배경 제외 여부를 설정합니다. |
| `separatesSharedBackground` | `Boolean` | `false` | iOS 26+ 이상에서 양쪽 그룹의 배경은 유지하면서 시스템 분리자를 삽입합니다. |
| `onClick` | `() -> Unit` | **필수** | 액션 버튼이 클릭/탭되었을 때 실행할 콜백입니다. `FlexibleSpace`에서는 무시됩니다. |

#### 팩토리 헬퍼 메서드
- **`InputAction.FlexibleSpace`**: 버튼 간 가변 간격을 배치하는 기본 스페이서.
- **`InputAction.FlexibleSpace(separatesSharedBackground: Boolean = false)`**: 양쪽 그룹의 배경을 유지하면서 시스템 분리자를 추가하는 가변 스페이서. iOS 26+에서는 `UIBarButtonItem.fixedSpaceItem()`을 사용합니다.

`hidesSharedBackground`는 해당 항목의 공유 배경을 실제로 숨기는 옵션입니다. 배경을 유지한
채 그룹만 나누려면 `separatesSharedBackground`를 사용하세요.

---

### 4. `InputActionStyle` (Enum)

`InputAction` 항목의 시각적 형태 및 표시 스타일을 정의합니다.

| 값 | 설명 |
| :--- | :--- |
| `Plain` | 일반적인 텍스트/아이콘 버튼 스타일입니다. |
| `Done` | 두껍고 강조된 완료/확인 작업 버튼 스타일입니다. |

---

### 5. `InputActionIcon`

플랫폼 네이티브 아이콘을 지정하기 위한 래퍼 클래스입니다.

```kotlin
data class InputActionIcon(
    val systemName: String,
)
```

| 프로퍼티 | 타입 | 설명 |
| :--- | :--- | :--- |
| `systemName` | `String` | iOS SF Symbol 아이콘 식별자 (예: `"chevron.down"`, `"chevron.up"`, `"xmark.circle"`, `"checkmark"` 등). |

---

## 사용 예제

`commonMain` 영역에서 Compose UI 트리를 `InputActionsHost`로 감싸고, 각 텍스트 필드 Modifier에 `inputActions`를 설정합니다:

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import zone.ien.inputactions.InputAction
import zone.ien.inputactions.InputActionIcon
import zone.ien.inputactions.InputActionStyle
import zone.ien.inputactions.InputActionsHost
import zone.ien.inputactions.inputActions

@Composable
fun RegistrationScreen() {
    val nameFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    InputActionsHost {
        Column {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .focusRequester(nameFocusRequester)
                    .inputActions(
                        InputAction(
                            title = "다음",
                            icon = InputActionIcon(systemName = "chevron.down"),
                            onClick = { emailFocusRequester.requestFocus() },
                        ),
                        InputAction.FlexibleSpace(separatesSharedBackground = true),
                        InputAction(
                            title = "완료",
                            style = InputActionStyle.Done,
                            onClick = { focusManager.clearFocus() },
                        ),
                    ),
                label = { Text("이름") },
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .focusRequester(emailFocusRequester)
                    .inputActions(
                        InputAction(
                            title = "이전",
                            icon = InputActionIcon(systemName = "chevron.up"),
                            onClick = { nameFocusRequester.requestFocus() },
                        ),
                        InputAction.FlexibleSpace,
                        InputAction(
                            title = "완료",
                            style = InputActionStyle.Done,
                            onClick = { focusManager.clearFocus() },
                        ),
                    ),
                label = { Text("이메일") },
            )
        }
    }
}
```

---

## 예제 앱 실행 방법

저장소의 `example/` 디렉터리에 Kotlin Multiplatform Compose 샘플 애플리케이션이 포함되어 있습니다.

### Android
```bash
./gradlew :example:androidApp:installDebug
```

### iOS
`example/iosApp/iosApp.xcodeproj` 파일을 Xcode에서 열고 시뮬레이터 또는 실기기 타깃을 선택하여 실행합니다.

---

## 플랫폼 제약 사항

1. **Compose iOS Text Responder**: iOS 네이티브 키보드 액세서리 연동은 Compose Multiplatform의 네이티브 UIResponder 텍스트 편집 인프라에 의존합니다.
2. **UIKit 계층 순회**: 네이티브 액세서리 바는 현재 활성화된 Responder 뷰에 동적으로 부착됩니다.
3. **Android 처리**: Android는 네이티브 소프트웨어 키패드가 IME 액션 버튼을 직접 처리하므로, 기본 `KeyboardOptions(imeAction = ...)` 매커니즘을 사용하도록 안심하고 위임할 수 있습니다.

---

## 라이선스

```
Copyright (c) 2026. Compose Input Actions project and open source contributors.
Copyright (c) 2026. IENGROUND of IENLAB.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
