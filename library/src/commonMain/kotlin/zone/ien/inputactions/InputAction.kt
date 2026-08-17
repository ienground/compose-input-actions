package zone.ien.inputactions

/**
 * Describes how an input action is presented in a native action bar.
 */
public enum class InputActionStyle {
    /** Displays the action using the platform's regular button style. */
    Plain,

    /** Displays the action using the platform's emphasized completion style. */
    Done,

    /** Inserts a flexible spacer instead of a tappable button. */
    FlexibleSpace,
}

/**
 * Describes a platform-native icon for an [InputAction].
 *
 * On iOS, [systemName] is resolved as an SF Symbol name. Other platforms may use the same
 * identifier with their native icon system or fall back to the action title.
 */
public data class InputActionIcon(
    public val systemName: String,
)

/**
 * Represents an action attached to a text input field.
 *
 * @property title Human-readable action label shown in the platform accessory area or action bar.
 *   It may be empty for icon-only actions and is ignored for [InputActionStyle.FlexibleSpace].
 * @property icon Optional platform-native icon shown instead of the title when supported.
 * @property style Presentation style used by the native action bar.
 * @property onClick Callback invoked when the user triggers this input action. It is ignored for
 *   [InputActionStyle.FlexibleSpace].
 */
public class InputAction private constructor(
    public val title: String,
    public val style: InputActionStyle,
    public val onClick: () -> Unit,
    public val icon: InputActionIcon?,
) {
    /**
     * Creates a title-based input action.
     *
     * This overload preserves the original [InputAction] call shape.
     */
    public constructor(
        title: String = "",
        style: InputActionStyle = InputActionStyle.Plain,
        onClick: () -> Unit,
    ) : this(title, style, onClick, null)

    /**
     * Creates an input action with an optional accessible title and a native icon.
     *
     * When [title] is empty, the platform may use the icon identifier as an accessibility label.
     */
    public constructor(
        title: String = "",
        icon: InputActionIcon,
        style: InputActionStyle = InputActionStyle.Plain,
        onClick: () -> Unit,
    ) : this(title, style, onClick, icon)

    public companion object {
        /**
         * Creates a flexible spacer for placement between visible input actions.
         *
         * The spacer itself has no visible title or callback. Add visible actions separately when
         * constructing the action list.
         */
        public val FlexibleSpace: InputAction
            get() = InputAction(
                title = "",
                style = InputActionStyle.FlexibleSpace,
                onClick = {},
            )
    }
}
