package zone.ien.utils.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import zone.ien.inputactions.InputAction
import zone.ien.inputactions.InputActionIcon
import zone.ien.inputactions.InputActionStyle
import zone.ien.inputactions.InputActionsHost
import zone.ien.inputactions.InputActionsStyle
import zone.ien.inputactions.inputActions

@Composable
@Preview
fun App() {
    MaterialTheme {
        InputActionsHost {
            InputActionsSample()
        }
    }
}

@Composable
private fun InputActionsSample() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var focusedField by remember { mutableStateOf("없음") }
    var lastAction by remember { mutableStateOf("없음") }

    val nameFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val nameActions = remember(
        nameFocusRequester,
        emailFocusRequester,
        focusManager,
        keyboardController,
    ) {
        listOf(
            InputAction(
                title = "Next",
                icon = InputActionIcon(systemName = "chevron.down"),
            ) {
                lastAction = "이름: Next"
                emailFocusRequester.requestFocus()
            },
            InputAction(
                title = "Clear",
                icon = InputActionIcon(systemName = "xmark.circle"),
            ) {
                lastAction = "이름: Clear"
                name = ""
            },
            InputAction.FlexibleSpace,
            InputAction(
                title = "Done",
                icon = InputActionIcon(systemName = "checkmark"),
                style = InputActionStyle.Done,
                onClick = {
                    lastAction = "이름: Done"
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
            ),
        )
    }
    val emailActions = remember(
        email.isNotEmpty(),
        nameFocusRequester,
        focusManager,
        keyboardController,
    ) {
        buildList {
            add(
                InputAction(
                    title = "Previous",
                    icon = InputActionIcon(systemName = "chevron.up"),
                ) {
                    lastAction = "이메일: Previous"
                    nameFocusRequester.requestFocus()
                },
            )
            if (email.isNotEmpty()) {
                add(
                    InputAction(
                        title = "Clear",
                        icon = InputActionIcon(systemName = "xmark.circle"),
                    ) {
                        lastAction = "이메일: Clear"
                        email = ""
                    },
                )
            }
            add(InputAction.FlexibleSpace)
            add(
                InputAction(
                    title = "Done",
                    icon = InputActionIcon(systemName = "checkmark"),
                    style = InputActionStyle.Done,
                    onClick = {
                        lastAction = "이메일: Done"
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                )
            )
        }
    }

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Compose Input Actions",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "포커스된 TextField의 iOS 키보드 상단에 UIToolbar를 표시하는 예제입니다.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.inputActions(
                    InputAction.FlexibleSpace,
                    InputAction(
                        title = "Hello",
                        style = InputActionStyle.Plain,
                        onClick = {},
                    ),

//                    InputAction(
//                        title = "Hello",
//                        style = InputActionStyle.Done,
//                        onClick = {},
//                    ),
                )
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            focusedField = "이름"
                        }
                    }
                    .inputActions(
                        *nameActions.toTypedArray(),
                        style = InputActionsStyle.Pill,
                    ),
                label = { Text("이름") },
                placeholder = { Text("Next / Clear 액션") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            focusedField = "이메일"
                        }
                    }
                    .inputActions(*emailActions.toTypedArray()),
                label = { Text("이메일") },
                placeholder = { Text("Previous / Clear / Done 액션") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )

            Text(
                text = "현재 포커스: $focusedField",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "마지막 액션: $lastAction",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "이름 액션: ${nameActions.joinToString { it.title }}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "이메일 액션: ${emailActions.joinToString { it.title }}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "iOS에서는 위 액션들이 키보드 상단 toolbar 버튼으로 표시됩니다. Android 호스트는 no-op입니다.",
                style = MaterialTheme.typography.bodySmall,
            )

            TextButton(
                onClick = {
                    name = ""
                    email = ""
                    focusedField = "없음"
                    lastAction = "초기화"
                    focusManager.clearFocus()
                },
            ) {
                Text(text = "상태 초기화")
            }
        }
    }
}
