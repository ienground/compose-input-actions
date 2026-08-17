package zone.ien.inputactions

/**
 * Identifies one Compose element that owns a set of input actions.
 */
internal class InputActionTarget

/**
 * Keeps the actions for composed text-input targets and the currently active target.
 */
internal class InputActionRegistry {
    private val actionsByTarget = mutableMapOf<InputActionTarget, List<InputAction>>()
    private var activeTarget: InputActionTarget? = null
    private var activeStyle: InputActionsStyle = InputActionsStyle.Toolbar

    fun register(
        target: InputActionTarget,
        actions: List<InputAction>,
        style: InputActionsStyle = InputActionsStyle.Toolbar,
    ) {
        actionsByTarget[target] = actions.toList()
        activeTarget = target
        activeStyle = style
    }

    fun unregister(target: InputActionTarget) {
        actionsByTarget.remove(target)
        if (activeTarget === target) {
            activeTarget = null
            activeStyle = InputActionsStyle.Toolbar
        }
    }

    fun activeActions(): List<InputAction> {
        return activeTarget?.let(actionsByTarget::get).orEmpty()
    }

    fun activeStyle(): InputActionsStyle {
        return activeStyle
    }

    fun clear() {
        actionsByTarget.clear()
        activeTarget = null
        activeStyle = InputActionsStyle.Toolbar
    }
}
/**
 * Common host implementation used by platforms that do not expose a native accessory yet.
 */
internal open class CommonInputActionsHost : InputActionHost {
    protected val registry = InputActionRegistry()

    override fun registerActions(
        target: InputActionTarget,
        actions: List<InputAction>,
        style: InputActionsStyle,
    ) {
        registry.register(target, actions, style)
    }

    override fun unregisterActions(target: InputActionTarget) {
        registry.unregister(target)
    }

    override fun dispose() {
        registry.clear()
    }
}
