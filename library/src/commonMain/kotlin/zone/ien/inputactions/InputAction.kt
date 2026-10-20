package zone.ien.inputactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Describes how an input action is presented in a native action bar.
 */
public enum class InputActionStyle {
    /** Displays the action using the platform's regular button style. */
    Plain,

    /** Displays the action using the platform's emphasized completion style. */
    Done,
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
 *   It may be empty for icon-only actions and is ignored for flexible spaces.
 * @property icon Optional platform-native icon shown instead of the title when supported.
 * @property hidesSharedBackground Whether the native action item should opt out of the shared
 *   toolbar background when the platform supports that behavior.
 * @property separatesSharedBackground Whether a zero-width native separator should be inserted
 *   next to this item so adjacent action groups retain their own backgrounds.
 * @property style Presentation style used by the native action bar.
 * @property isFlexibleSpace Whether this item represents a flexible spacer instead of a tappable
 *   action button.
 * @property onClick Callback invoked when the user triggers this input action. It is ignored for
 *   flexible spaces.
 */
public class InputAction internal constructor(
    public val title: String = "",
    public val style: InputActionStyle = InputActionStyle.Plain,
    public val icon: InputActionIcon? = null,
    public val hidesSharedBackground: Boolean = false,
    public val separatesSharedBackground: Boolean = false,
    public val isFlexibleSpace: Boolean = false,
    public val onClick: () -> Unit,
) {
    public constructor(
        title: String = "",
        style: InputActionStyle = InputActionStyle.Plain,
        icon: InputActionIcon? = null,
        hidesSharedBackground: Boolean = false,
        separatesSharedBackground: Boolean = false,
        onClick: () -> Unit,
    ) : this(
        title = title,
        style = style,
        icon = icon,
        hidesSharedBackground = hidesSharedBackground,
        separatesSharedBackground = separatesSharedBackground,
        isFlexibleSpace = false,
        onClick = onClick,
    )

    public companion object {
        /**
         * 누르면 현재 소프트웨어 키보드를 닫는 액션을 생성합니다.
         *
         * 키보드 컨트롤러가 없는 환경에서는 클릭해도 아무 동작을 하지 않습니다.
         * 입력 필드의 포커스는 해제하지 않습니다.
         *
         * @param icon 표시할 아이콘. 기본값은 체크 아이콘입니다.
         * @param style 액션의 표시 스타일. 기본값은 완료 스타일입니다.
         */
        @Composable
        public fun Done(
            icon: InputActionIcon? = InputActionIcon("checkmark"),
            style: InputActionStyle = InputActionStyle.Done,
        ): InputAction {
            val keyboardController = LocalSoftwareKeyboardController.current
            return remember(keyboardController, icon, style) {
                InputAction(
                    icon = icon,
                    style = style,
                    onClick = { keyboardController?.hide() },
                )
            }
        }

        /**
         * Creates a flexible spacer for placement between visible input actions.
         *
         * The spacer itself has no visible title or callback. Add visible actions separately when
         * constructing the action list.
         */
        public val FlexibleSpace: InputAction
            get() = FlexibleSpace()

        /**
         * Creates a flexible spacer with explicit shared-background behavior.
         *
         * On iOS 26 and later, setting [separatesSharedBackground] to `true` inserts the native
         * zero-width separator that keeps the groups on either side visually separate.
         */
        public fun FlexibleSpace(
            hidesSharedBackground: Boolean = false,
            separatesSharedBackground: Boolean = false,
        ): InputAction = InputAction(
            title = "",
            style = InputActionStyle.Plain,
            icon = null,
            hidesSharedBackground = hidesSharedBackground,
            separatesSharedBackground = separatesSharedBackground,
            isFlexibleSpace = true,
            onClick = {},
        )

        /**
         * Kotlin-style lowercase alias for [FlexibleSpace].
         */
        public fun flexibleSpace(
            hidesSharedBackground: Boolean = false,
            separatesSharedBackground: Boolean = false,
        ): InputAction = FlexibleSpace(
            hidesSharedBackground = hidesSharedBackground,
            separatesSharedBackground = separatesSharedBackground,
        )
    }
}
