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
 * Represents an action attached to a text input field.
 *
 * @property title Human-readable action label shown in the platform accessory area or action bar.
 *   It is ignored for [InputActionStyle.FlexibleSpace].
 * @property style Presentation style used by the native action bar.
 * @property onClick Callback invoked when the user triggers this input action. It is ignored for
 *   [InputActionStyle.FlexibleSpace].
 */
public class InputAction(
    public val title: String,
    public val style: InputActionStyle = InputActionStyle.Plain,
    public val onClick: () -> Unit,
) {
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
