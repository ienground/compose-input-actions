package zone.ien.inputactions

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf

/**
 * Controls how the active input actions are presented by a platform integration.
 */
public enum class InputActionsStyle {
    /** Uses the platform's standard input-action toolbar. */
    Toolbar,

    /**
     * Requests the platform's shared-background pill presentation when available.
     *
     * On iOS 26 and later, action buttons use UIKit's shared toolbar background group.
     */
    Pill,
}

/**
 * Adds native text-input actions that become active while this element owns text input focus.
 *
 * On iOS, actions are presented using the native text-input responder associated with the
 * focused Compose text field.
 *
 * @param actions The list of [InputAction]s to present when this field is focused.
 * @param style Presentation style for the active input actions.
 */
public fun Modifier.inputActions(
    vararg actions: InputAction,
    style: InputActionsStyle = InputActionsStyle.Toolbar,
): Modifier = this then InputActionsElement(actions.toList(), style)

private data class InputActionsElement(
    val actions: List<InputAction>,
    val style: InputActionsStyle,
) : ModifierNodeElement<InputActionsNode>() {
    override fun create(): InputActionsNode {
        return InputActionsNode(actions, style)
    }

    override fun update(node: InputActionsNode) {
        node.updateActions(actions, style)
    }
}

private class InputActionsNode(
    private var actions: List<InputAction>,
    private var style: InputActionsStyle,
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
            host?.registerActions(target, actions, style)
        } else {
            host?.unregisterActions(target)
        }
    }

    fun updateActions(
        newActions: List<InputAction>,
        newStyle: InputActionsStyle,
    ) {
        if (actions == newActions && style == newStyle) {
            return
        }

        actions = newActions
        style = newStyle
        if (isFocused) {
            host?.registerActions(target, actions, style)
        }
    }
}
