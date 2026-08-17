package zone.ien.inputactions

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf

/**
 * Adds native text-input actions that become active while this element owns text input focus.
 *
 * On iOS, actions are presented using the native text-input responder associated with the
 * focused Compose text field.
 *
 * @param actions The list of [InputAction]s to present when this field is focused.
 */
public fun Modifier.inputActions(
    vararg actions: InputAction,
): Modifier = this then InputActionsElement(actions.toList())

private data class InputActionsElement(
    val actions: List<InputAction>,
) : ModifierNodeElement<InputActionsNode>() {
    override fun create(): InputActionsNode {
        return InputActionsNode(actions)
    }

    override fun update(node: InputActionsNode) {
        node.updateActions(actions)
    }
}

private class InputActionsNode(
    private var actions: List<InputAction>,
) : Modifier.Node(),
    FocusEventModifierNode,
    CompositionLocalConsumerModifierNode {
    private val target = InputActionTarget()
    private var host: InputActionHost? = null
    private var isFocused = false

    override fun onAttach() {
        host = currentValueOf(LocalInputActionsHost)
    }

    override fun onDetach() {
        host?.unregisterActions(target)
        host = null
        isFocused = false
    }

    override fun onFocusEvent(focusState: FocusState) {
        isFocused = focusState.isFocused
        if (isFocused) {
            host?.registerActions(target, actions)
        } else {
            host?.unregisterActions(target)
        }
    }

    fun updateActions(
        newActions: List<InputAction>,
    ) {
        if (actions == newActions) {
            return
        }

        actions = newActions
        if (isFocused) {
            host?.registerActions(target, actions)
        }
    }
}
