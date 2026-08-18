package zone.ien.inputaction.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import composeinputactions.example.composeapp.generated.resources.Res
import composeinputactions.example.composeapp.generated.resources.app_name
import composeinputactions.example.composeapp.generated.resources.button_done
import composeinputactions.example.composeapp.generated.resources.button_next
import composeinputactions.example.composeapp.generated.resources.button_prev
import composeinputactions.example.composeapp.generated.resources.section_pill_icon_buttons
import composeinputactions.example.composeapp.generated.resources.section_seperate_icon_buttons
import composeinputactions.example.composeapp.generated.resources.section_seperate_text_buttons
import composeinputactions.example.composeapp.generated.resources.section_single_text_button
import composeinputactions.example.composeapp.generated.resources.section_standard_icon_buttons
import composeinputactions.example.composeapp.generated.resources.section_standard_text_buttons
import org.jetbrains.compose.resources.stringResource
import zone.ien.inputactions.InputAction
import zone.ien.inputactions.InputActionIcon
import zone.ien.inputactions.InputActionStyle
import zone.ien.inputactions.InputActionsHost
import zone.ien.inputactions.inputActions
import zone.ien.utils.ui.foundation.IenTheme
import zone.ien.utils.ui.interactive.IenTextField
import zone.ien.utils.ui.primitives.IenDivider
import zone.ien.utils.ui.primitives.IenSurface
import zone.ien.utils.ui.primitives.IenText
import zone.ien.utils.ui.screen.IenScaffoldContentEdge
import zone.ien.utils.ui.screen.IenTopAppBarScaffold
import zone.ien.utils.ui.wrapper.IenRootWrapper

@Composable
fun App() {
    IenRootWrapper {
        IenTheme {
            InputActionsHost {
                val scrollState = rememberScrollState()
                IenTopAppBarScaffold(
                    actions = listOf(),
                    contentEdge = IenScaffoldContentEdge(scrollState = scrollState),
                    title = {
                        Text(text = stringResource(Res.string.app_name))
                    },
                    modifier = Modifier
                ) {
                    SampleScreenBody(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .padding(it).imePadding()
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleScreenBody(
    modifier: Modifier = Modifier
) {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var text3 by remember { mutableStateOf("") }
    var text4 by remember { mutableStateOf("") }
    var text5 by remember { mutableStateOf("") }
    var text6 by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 텍스트 버튼 1
    // 텍스트 버튼 2
    // 아이콘
    // 아이콘 연결 배경 제거

    val iconActions = arrayOf(
        InputAction(
            title = stringResource(Res.string.button_prev),
            icon = InputActionIcon("chevron.up")
        ) {
            focusManager.moveFocus(FocusDirection.Previous)
        },
        InputAction(
            title = stringResource(Res.string.button_next),
            icon = InputActionIcon("chevron.down")
        ) {
            focusManager.moveFocus(FocusDirection.Next)
        },
        InputAction(
            title = stringResource(Res.string.button_done),
            style = InputActionStyle.Done,
            icon = InputActionIcon("checkmark")
        ) {
            keyboardController?.hide()
        },
    )

    Column(
        modifier = modifier
    ) {
        ComponentSection(
            title = stringResource(Res.string.section_standard_text_buttons)
        ) {
            IenTextField(
                value = text1,
                onValueChange = { text1 = it },
                modifier = Modifier
                    .inputActions(
                        InputAction(
                            title = stringResource(Res.string.button_prev),
                        ) {
                            focusManager.moveFocus(FocusDirection.Previous)
                        },
                        InputAction(
                            title = stringResource(Res.string.button_next),
                        ) {
                            focusManager.moveFocus(FocusDirection.Next)
                        },
                        InputAction(
                            title = stringResource(Res.string.button_done),
                            style = InputActionStyle.Done
                        ) {
                            keyboardController?.hide()
                        },
                    )
            )
        }
        ComponentSection(
            title = stringResource(Res.string.section_seperate_text_buttons)
        ) {
            IenTextField(
                value = text2,
                onValueChange = { text2 = it },
                modifier = Modifier
                    .inputActions(
                        InputAction(
                            title = stringResource(Res.string.button_prev),
                        ) {
                            focusManager.moveFocus(FocusDirection.Previous)
                        },
                        InputAction.FlexibleSpace,
                        InputAction(
                            title = stringResource(Res.string.button_next),
                        ) {
                            focusManager.moveFocus(FocusDirection.Next)
                        },
                        InputAction(
                            title = stringResource(Res.string.button_done),
                            style = InputActionStyle.Done
                        ) {
                            keyboardController?.hide()
                        },
                    )
            )
        }
        ComponentSection(
            title = stringResource(Res.string.section_single_text_button)
        ) {
            IenTextField(
                value = text3,
                onValueChange = { text3 = it },
                modifier = Modifier
                    .inputActions(
                        InputAction(
                            title = stringResource(Res.string.button_done),
                        ) {
                            keyboardController?.hide()
                        },
                    )
            )
        }
        ComponentSection(
            title = stringResource(Res.string.section_standard_icon_buttons)
        ) {
            IenTextField(
                value = text4,
                onValueChange = { text4 = it },
                modifier = Modifier
                    .inputActions(*iconActions)
            )
        }
        ComponentSection(
            title = stringResource(Res.string.section_pill_icon_buttons)
        ) {
            IenTextField(
                value = text5,
                onValueChange = { text5 = it },
                modifier = Modifier
                    .inputActions(
                        InputAction(
                            title = stringResource(Res.string.button_prev),
                            icon = InputActionIcon("chevron.up"),
                        ) {
                            focusManager.moveFocus(FocusDirection.Previous)
                        },
                        InputAction(
                            title = stringResource(Res.string.button_next),
                            icon = InputActionIcon("chevron.down"),
                        ) {
                            focusManager.moveFocus(FocusDirection.Next)
                        },
                        InputAction(
                            title = stringResource(Res.string.button_done),
                            icon = InputActionIcon("checkmark")
                        ) {
                            keyboardController?.hide()
                        },
                    )
            )
        }
        ComponentSection(
            title = stringResource(Res.string.section_seperate_icon_buttons)
        ) {
            IenTextField(
                value = text6,
                onValueChange = { text6 = it },
                modifier = Modifier
                    .inputActions(
                        InputAction(
                            title = stringResource(Res.string.button_prev),
                            icon = InputActionIcon("chevron.up"),
                        ) {
                            focusManager.moveFocus(FocusDirection.Previous)
                        },
                        InputAction(
                            title = stringResource(Res.string.button_next),
                            icon = InputActionIcon("chevron.down"),
                            separatesSharedBackground = true
                        ) {
                            focusManager.moveFocus(FocusDirection.Next)
                        },
                        InputAction(
                            title = stringResource(Res.string.button_done),
                            icon = InputActionIcon("checkmark")
                        ) {
                            keyboardController?.hide()
                        },
                    )
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenPreview() {
    IenTheme {
        SampleScreenBody()
    }
}

@Composable
private fun ComponentSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    IenSurface(
        modifier = Modifier.fillMaxWidth(),
        color = IenTheme.colors.surface,
        tonalElevation = IenTheme.elevation.raised,
    ) {
        Column(
            modifier = Modifier.padding(IenTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(IenTheme.spacing.md),
        ) {
            IenText(title, style = IenTheme.typography.title3)
            IenDivider()
            content()
        }
    }
}
