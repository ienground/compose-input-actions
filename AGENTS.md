# AGENTS.md

## Project Overview

`zone.ien.inputactions` is a Kotlin Multiplatform library that brings native platform text-input actions and accessories to Compose Multiplatform `TextField`s.

The initial focus is iOS.

The library should allow developers to attach native iOS input actions, such as keyboard accessory actions, to ordinary Compose `TextField` / `BasicTextField` instances without replacing them with UIKit text fields.

Example target API:

```kotlin
TextField(
    value = value,
    onValueChange = { value = it },
    modifier = Modifier.inputActions(
        InputAction(
            title = "Done",
            onClick = {
                // ...
            },
        ),
    ),
)
```

The exact public API is not fixed yet. Prefer an API that feels idiomatic in Compose rather than exposing UIKit implementation details.

---

## Package

Use the following root package:

```text
zone.ien.inputactions
```

Do not introduce another root namespace.

Subpackages may be introduced only when they provide meaningful separation.

For example:

```text
zone.ien.inputactions
zone.ien.inputactions.internal
zone.ien.inputactions.platform
```

Avoid unnecessary package fragmentation.

---

## Technology Baseline

This is a Kotlin Multiplatform / Compose Multiplatform library.

Target the Compose Multiplatform 1.12.x generation.

If the project is already configured with a specific pre-release version such as `1.12.0-rc01`, preserve that version unless the task explicitly requires changing it.

Do not downgrade Kotlin, Compose Multiplatform, or related dependencies just to work around an implementation problem.

Before using APIs whose availability depends on the Compose version, inspect the actual project dependency versions.

Primary technologies:

- Kotlin Multiplatform
- Compose Multiplatform
- Kotlin/Native
- UIKit interop on iOS
- Gradle Kotlin DSL
- Version Catalog when already present

Prefer official Kotlin and Compose APIs whenever possible.

---

## Supported Platforms

The architecture must remain Kotlin Multiplatform-friendly.

The primary implementation target is currently:

```text
commonMain
└── iosMain
```

Do not create platform modules merely for architectural symmetry.

If all platform-specific implementation can live naturally in `iosMain`, keep the project simple.

Other platforms may initially provide:

- no-op behavior,
- unsupported behavior documented at compile time,
- or their own implementation later.

Avoid designing the common API around UIKit-specific types.

For example, this is undesirable:

```kotlin
fun Modifier.inputAccessoryView(
    view: UIView,
): Modifier
```

Prefer platform-neutral concepts:

```kotlin
fun Modifier.inputActions(
    vararg actions: InputAction,
): Modifier
```

UIKit objects should normally remain inside `iosMain`.

---

## Core Design Principle

A Compose `TextField` is not a UIKit `UITextField`.

Do not assume that every Compose `TextField` has a corresponding `UITextField` instance.

On modern Compose Multiplatform for iOS, native text editing is backed by Compose's iOS text-input infrastructure using a native `UIView` implementing UIKit text-input protocols such as:

```text
UITextInput
UIKeyInput
```

Therefore, implementations must target the actual Compose/iOS responder architecture rather than assuming:

```text
Compose TextField == UITextField
```

This distinction is fundamental to this project.

---

## Architecture Direction

The preferred architecture is:

```text
Compose TextField
        │
        │ Modifier.inputActions(...)
        ▼
Compose-side registration
        │
        │ focus state
        ▼
Input Action Host / Registry
        │
        ▼
iOS implementation
        │
        ▼
active native text-input UIResponder
        │
        ▼
native input actions / accessory UI
```

A `TextField` should describe its desired actions.

It should not need to know how UIKit displays them.

---

## Compose API

Prefer Modifier-based APIs for functionality associated with a particular text field.

Preferred direction:

```kotlin
TextField(
    value = text,
    onValueChange = { text = it },
    modifier = Modifier.inputActions(
        InputAction.Done {
            // ...
        },
    ),
)
```

Possible supporting APIs may include:

```kotlin
InputAction
InputActionScope
InputActionsHost
LocalInputActionsHost
```

Names are not final.

Before introducing a public type, consider:

1. Does the caller actually need this abstraction?
2. Can it remain internal?
3. Does the API feel like Compose?
4. Does it leak UIKit implementation details?
5. Will the API still make sense if Android/Desktop support is added later?

Avoid forcing users to manually assign IDs to text fields.

If an internal identifier is necessary, generate and manage it inside the library.

For example:

```kotlin
val id = remember { InputActionId() }
```

rather than:

```kotlin
Modifier.inputActions(
    id = "email-field",
    ...
)
```

unless an explicit stable identifier provides an actual public feature.

---

## Focus Handling

Input actions belong to the currently focused text field.

The library should track focus through Compose APIs whenever possible.

Possible mechanisms include:

```kotlin
Modifier.onFocusChanged { ... }
```

or other appropriate focus APIs available in the project's Compose version.

Do not infer the active field solely by walking UIKit views if Compose can provide the information directly.

Compose should determine:

```text
Which field is active?
Which actions belong to it?
```

UIKit should determine:

```text
How should those actions be presented natively?
Which UIResponder currently owns text input?
```

Keep these responsibilities separate.

---

## CompositionLocal

A `CompositionLocal` may be used to connect individual fields with an input-action host.

Conceptually:

```kotlin
internal val LocalInputActionHost =
    staticCompositionLocalOf<InputActionHost?> {
        null
    }
```

Do not require users to manually provide a host unless there is no reliable way for the library to install one automatically.

Preferred UX:

```kotlin
TextField(
    modifier = Modifier.inputActions(...)
)
```

Acceptable UX if a host is technically required:

```kotlin
InputActionsHost {
    App()
}
```

Requiring modification of `MainViewController` should be treated as a last resort.

---

## iOS Native Text Input

Be extremely careful when interacting with the active UIKit responder.

Do not create a hidden/shared `UITextField` and make it first responder merely to display input actions.

That approach can compete with Compose's actual text-input responder and may interfere with:

- keyboard input,
- selection,
- cursor movement,
- autocorrection,
- text replacement,
- hardware keyboards,
- dictation,
- autofill,
- accessibility,
- IME behavior.

If a shared native object is required, it should preferably be a controller/host responsible for actions rather than a competing text-input field.

Conceptually:

```text
InputActionController
```

rather than:

```text
SharedUITextField
```

---

## UIResponder Handling

When native integration requires access to the active input responder:

- use public UIKit behavior where possible,
- minimize assumptions about Compose internal class names,
- do not depend on private UIKit APIs,
- do not depend on memory-layout assumptions,
- do not use Objective-C runtime tricks unless there is no reasonable alternative.

If runtime traversal is required, isolate it behind a small internal interface.

For example:

```kotlin
internal interface NativeTextInputResolver {
    fun resolve(): NativeTextInputHandle?
}
```

The rest of the library must not depend on the details of Compose's internal UIKit hierarchy.

---

## Compose Internals

Do not copy or fork Compose internals without a strong reason.

Before depending on an internal Compose implementation:

1. Check whether a public API can solve the problem.
2. Check whether UIKit's responder system can solve the problem.
3. Inspect the current Compose Multiplatform source.
4. Identify exactly which internal behavior would be depended upon.
5. Isolate that dependency.
6. Document why the dependency exists.

Any dependency on:

```kotlin
internal
```

Compose APIs or implementation-specific UIKit classes must be treated as unstable.

Never spread such dependencies throughout the codebase.

Use a dedicated compatibility layer.

For example:

```text
iosMain/
  internal/
    ComposeTextInputBridge.kt
```

---

## UIKit Interop

UIKit-specific code belongs in `iosMain`.

Prefer typed Kotlin/Native interop.

Examples of APIs that may be relevant include:

```text
UIResponder
UIView
UIViewController
UIInputView
UIInputViewController
UIBarButtonItem
UIToolbar
UITextInput
UIKeyInput
UIKeyCommand
```

The exact mechanism must be selected based on the current Compose implementation rather than assumptions.

Do not expose UIKit classes from `commonMain` unless interoperability itself is explicitly part of the public API.

---

## Native Accessory Strategy

The primary research problem of this library is attaching actions/accessories to the native text-input responder used by Compose.

Possible approaches should be evaluated in roughly this order:

1. Public Compose API
2. Public UIKit responder API
3. Supported UIKit view/controller composition
4. Limited Compose implementation bridge
5. Runtime interception only as a final fallback

Do not immediately choose:

- method swizzling,
- isa-swizzling,
- private selectors,
- private UIKit classes,
- reflection-like runtime hacks.

If one of these becomes necessary, stop and document the trade-off before implementing it.

---

## API Stability

Treat all public declarations as long-term API commitments.

Keep experimental implementation details internal.

During early development, experimental public APIs may be annotated appropriately.

Do not make a class public merely because tests or another source set need access to it.

Prefer:

```kotlin
internal
```

by default.

Only promote something to `public` when it represents the intended consumer API.

---

## Naming

Use concise names that match Compose conventions.

Prefer:

```kotlin
Modifier.inputActions(...)
InputAction
InputActionsHost
```

Avoid redundant names such as:

```kotlin
ComposeTextFieldNativeInputActionModifier
IosUITextFieldActionManager
```

The package already provides context.

UIKit-specific internal names may be explicit where that improves clarity.

For example:

```kotlin
IosInputActionController
IosTextInputResponderResolver
```

---

## Kotlin Style

Follow idiomatic modern Kotlin.

Prefer:

- immutable state,
- expression-oriented code where readable,
- sealed hierarchies where they model a closed domain,
- value classes for lightweight identifiers when useful,
- named arguments for ambiguous calls,
- trailing commas,
- explicit visibility for important declarations.

Avoid:

- unnecessary abstract factories,
- Java-style builders,
- singleton managers without lifecycle ownership,
- deeply nested callbacks,
- unnecessary mutable collections.

Use coroutines only when the operation is genuinely asynchronous.

Do not introduce coroutines simply to defer UIKit work to the main thread if a direct main-thread mechanism is more appropriate.

---

## Documentation

All public Kotlin APIs must have KDoc.

KDoc should explain behavior and constraints, not restate the declaration.

Example:

```kotlin
/**
 * Adds native text-input actions that become active while this element owns text input focus.
 *
 * On iOS, actions are presented using the native text-input responder associated with the
 * focused Compose text field.
 */
fun Modifier.inputActions(
    vararg actions: InputAction,
): Modifier
```

Document platform-specific behavior where it affects consumers.

Do not mention implementation details that are not part of the API contract.

---

## Threading

UIKit operations must run on the iOS main thread.

Do not perform UIKit hierarchy traversal or responder manipulation from background threads.

When scheduling is required, use platform-appropriate main-thread execution.

Avoid arbitrary delays such as:

```kotlin
delay(100)
```

to wait for focus or keyboard state.

If ordering matters, identify the actual lifecycle/event boundary and synchronize against that instead.

---

## Lifecycle

Avoid retaining:

- `UIViewController`,
- `UIView`,
- `UIResponder`,
- Compose nodes,
- callbacks belonging to disposed compositions

longer than necessary.

Registrations associated with a Compose node must be removed when that node leaves composition.

Prefer lifecycle-aware registration through Modifier nodes or Compose effects.

Be especially careful with strong Kotlin/Native references crossing into UIKit objects.

---

## Modifier Implementation

For behavior tightly coupled to a Compose element, prefer modern Modifier node APIs when they provide better lifecycle or performance characteristics.

Avoid unnecessary recomposition-driven registration.

A field changing its text value must not cause its native action registration to be destroyed and recreated unless the actions themselves changed.

Callbacks should remain current without forcing expensive platform object reconstruction.

Use patterns equivalent to updated-state handling where appropriate.

---

## Performance

This library will commonly run on every focused text field in an application.

Therefore:

- do not scan the entire UIKit hierarchy on every recomposition,
- do not allocate native accessory objects on every keystroke,
- do not recreate action registries for ordinary value changes,
- cache reusable native structures where lifecycle-safe,
- perform responder lookup only when necessary.

Correctness takes priority over micro-optimization, but avoid obviously hot-path work.

---

## Accessibility

Native actions must remain accessible to:

- VoiceOver,
- Switch Control,
- Full Keyboard Access where applicable.

Provide meaningful accessibility labels for icon-only actions.

Do not remove or replace native text-input behavior merely to simplify implementation.

Preserving normal iOS text editing behavior is a core requirement.

---

## Hardware Keyboard

Do not assume that the software keyboard is visible whenever a text field is focused.

The implementation must remain valid with:

- hardware keyboards,
- iPad keyboard accessories,
- Mac Catalyst-like interaction patterns where applicable,
- software keyboard hidden while focus remains.

The concept is:

```text
text input focus
```

not:

```text
keyboard visibility
```

unless a feature explicitly requires keyboard visibility.

---

## Testing

Write tests for behavior that can be tested without UIKit in `commonTest`.

Examples:

- action registration,
- active field switching,
- registry cleanup,
- action replacement,
- identifier behavior.

Platform integration tests should cover iOS behavior where practical.

Important scenarios include:

1. One text field receives focus.
2. Focus moves between two fields with different actions.
3. A focused field leaves composition.
4. Actions change while the field remains focused.
5. No actions are registered.
6. Multiple text fields exist in lazy layouts.
7. Software keyboard is dismissed while focus state changes.
8. Hardware keyboard is connected.
9. A dialog or navigation transition removes the active field.
10. Rapid focus switching does not leave stale actions.

Do not design the implementation around passing tests while ignoring actual UIKit lifecycle behavior.

---

## Source Structure

Start simple.

A reasonable initial layout is:

```text
src/
├── commonMain/
│   └── kotlin/
│       └── zone/ien/inputactions/
│           ├── InputAction.kt
│           └── InputActionsModifier.kt
│
├── iosMain/
│   └── kotlin/
│       └── zone/ien/inputactions/
│           └── ...
│
├── commonTest/
│   └── kotlin/
│       └── zone/ien/inputactions/
│
└── iosTest/
    └── kotlin/
        └── zone/ien/inputactions/
```

Do not split the repository into multiple Gradle modules until there is a concrete reason.

A single library module is preferred initially.

Potential reasons for introducing additional modules later include:

- a separately published UIKit integration artifact,
- a public testing artifact,
- genuinely independent platform integrations.

"Clean architecture" alone is not a sufficient reason.

---

## Dependency Policy

Keep dependencies minimal.

Do not add a dependency when the Kotlin standard library, Compose, or platform SDK already provides the required functionality.

Especially avoid adding dependencies for:

- lifecycle wrappers,
- simple registries,
- logging,
- identifiers,
- small collections utilities.

A low-level UI interoperability library should have a very small dependency surface.

---

## Logging

Do not leave `println` debugging in library code.

If internal diagnostic logging becomes necessary, isolate it and ensure release consumers do not receive noisy logs by default.

Never log:

- user-entered text,
- autofill values,
- clipboard contents,
- credentials,
- personal information.

---

## Error Handling

Prefer graceful absence over crashes for optional native integration.

For example, failure to resolve the native responder should generally result in no accessory being shown rather than crashing the application.

Use exceptions for actual programmer errors or violated API contracts.

Do not silently swallow conditions that indicate an internal invariant failure during development; assertions or diagnostic hooks may be appropriate.

---

## Example Application

If the repository includes a sample application, use it to demonstrate real scenarios rather than serving as a visual showcase.

At minimum, include examples for:

```text
Done action
Next / Previous fields
Multiple actions
Action with an icon
Dynamic actions
Multiple TextFields
```

The sample must use the same public API available to library consumers.

Do not access internal APIs from the sample to make demonstrations work.

---

## Changes to Build Configuration

When changing Gradle files:

1. Preserve existing version catalog conventions.
2. Do not downgrade dependency versions without explicit justification.
3. Avoid adding repositories beyond established trusted repositories.
4. Keep publishing configuration intact unless the task is specifically about publishing.
5. Do not introduce Android application configuration into a pure library module unnecessarily.

After dependency changes, verify the relevant targets compile.

---

## Compatibility Work

Compose's iOS text-input implementation may evolve.

Any workaround dependent on Compose internals must contain a comment describing:

- why it exists,
- what Compose behavior it relies on,
- what would indicate that it is no longer necessary.

Example:

```kotlin
// Compose iOS currently owns text input through a dedicated native UIResponder.
// Keep responder lookup isolated here because the concrete native view hierarchy
// is an implementation detail and may change between Compose releases.
```

Do not scatter version checks throughout unrelated code.

If version-specific behavior becomes necessary, centralize compatibility handling.

---

## Research Before Implementation

When investigating Compose internals, prefer authoritative sources in this order:

1. Current Compose Multiplatform source code
2. Official JetBrains Compose Multiplatform documentation
3. Current AndroidX Compose source where relevant
4. Apple UIKit documentation

Do not base implementation decisions solely on:

- Stack Overflow answers,
- old blog posts,
- assumptions from SwiftUI,
- assumptions from Android Compose,
- behavior observed on a single iOS release.

Verify the behavior against the Compose version actually used by this project.

---

## Git Changes

Keep commits focused.

Do not modify unrelated files.

Before considering a task complete:

```bash
git status
```

and inspect all changed files.

When staging manually, ensure newly created files are not accidentally omitted.

If the intended change is repository-wide and safe to stage together:

```bash
git add .
```

is acceptable.

Never commit generated build artifacts such as:

```text
build/
.gradle/
DerivedData/
```

---

## Agent Communication

Respond to the repository owner in Korean.

Code, API identifiers, commit messages, and technical terms may remain in English where natural.

When proposing architecture changes:

- explain the reason,
- identify dependencies on Compose/UIKit internals,
- call out compatibility risks,
- prefer the smallest viable implementation.

Do not create large design documents unless explicitly requested.

For normal implementation tasks, implement the solution instead of producing speculative documentation.

---

## Non-Goals

Unless explicitly requested, this project is not intended to:

- replace Compose `TextField` with `UIKitView`,
- implement an entirely new text editor,
- wrap `UITextField` as a Compose component,
- reimplement Compose text selection,
- manage keyboard visibility globally,
- provide a general UIKit interoperability framework.

The central goal is narrow:

> Allow ordinary Compose Multiplatform text fields to expose native platform text-input actions while preserving Compose ownership of the text field.

Keep the implementation centered on that goal.


---

## README.md Structure & Style Guide

When creating or updating the repository's `README.md`, follow the structure and design style established in `firebase-kotlin-sdk`:

1. **Header & Badges**
   - Centered top logo image `<p align="center"><img src="..." width="150" /></p>`.
   - Project title `# <Project Name>`.
   - Language switcher link (e.g. `**English** | [한국어](README_ko.md)`).
   - Badges for Maven/Sonatype, Kotlin version, and supported platforms (Android | iOS).
   - Concise 1-2 sentence core value proposition.

2. **Features & Support Matrix**
   - High-level feature highlights (Kotlin-First design, thin wrappers, etc.).
   - Detailed feature/supported matrix table with columns: `Feature`, `Android Support`, `iOS Support`, `Completion Rate`, and `Under the Hood`.

3. **Installation**
   - Gradle KMP dependency blocks (`build.gradle.kts` snippet).
   - Platform-Specific Setup instructions (Android configuration, iOS SPM / Xcode setup).
   - Callout boxes (e.g. `> [!IMPORTANT]`) for minimum toolchain requirements (e.g. Kotlin compiler versions).

4. **Running the Sample App**
   - Setup steps for sample/example applications, required setup files (e.g., config files or plists), and key demo components.

5. **Usage Example**
   - Practical, copy-pasteable Kotlin Multiplatform code snippets illustrating initialization and primary API usage.

6. **Migration Guide** (if applicable)
   - Target audience checklist.
   - Namespace mapping table comparing upstream / alternative libraries with this library.
   - Bullet points describing key API behavior changes (async/await models, event flows, platform defaults).

7. **Platform Limitations & Constraints**
   - Clear callouts for missing/partial platform APIs, memory actual fallbacks, or iOS/Android specific requirements.
   - Code examples showing how users can guard platform-specific logic.

8. **License**
   - Standard Apache 2.0 license code block with explicit copyright line.
