package zone.ien.inputactions

import androidx.compose.runtime.mutableStateOf
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIInputView
import platform.UIKit.UIInputViewStyle
import platform.UIKit.UIToolbar
import platform.UIKit.UIView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private const val INPUT_ACTION_SELECTOR = "invoke:"
private const val IOS26_MAJOR_VERSION = 26
private const val IOS26_TOOLBAR_BOTTOM_GAP = 8.0

/**
 * iOS host boundary for the native input accessory integration.
 *
 * Compose's platform text-input interceptor supplies the toolbar to the actual native responder;
 * this host only owns the active action snapshot and the UIKit accessory objects.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosInputActionsHost : CommonInputActionsHost() {
    private var actionsVersionState = mutableStateOf(0)
    internal val actionsVersion: Int
        get() = actionsVersionState.value

    private var toolbarTargets: List<IosInputActionTarget> = emptyList()
    private var currentToolbar: UIToolbar? = null

    override fun registerActions(target: InputActionTarget, actions: List<InputAction>) {
        super.registerActions(target, actions)
        actionsVersionState.value++
        currentToolbar = null
        toolbarTargets = emptyList()
    }

    override fun unregisterActions(target: InputActionTarget) {
        super.unregisterActions(target)
        actionsVersionState.value++
        currentToolbar = null
        toolbarTargets = emptyList()
    }

    override fun dispose() {
        actionsVersionState.value++
        currentToolbar = null
        toolbarTargets = emptyList()
        super.dispose()
    }

    internal fun createInputAccessoryView(): UIView? {
        val toolbar = createToolbar() ?: return null
        val majorVersion = NSProcessInfo.processInfo().operatingSystemVersion.useContents {
            majorVersion.toInt()
        }
        return wrapToolbarForIosVersion(toolbar, majorVersion)
    }

    internal fun createToolbar(): UIToolbar? {
        val actions = registry.activeActions()
        if (actions.isEmpty()) {
            currentToolbar = null
            toolbarTargets = emptyList()
            return null
        }

        val targets = actions
            .filterNot { it.style == InputActionStyle.FlexibleSpace }
            .map { action -> IosInputActionTarget(action.onClick) }
        var targetIndex = 0
        val buttons = actions.map { action ->
            if (action.style == InputActionStyle.FlexibleSpace) {
                UIBarButtonItem(
                    barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
                    target = null,
                    action = null,
                )
            } else {
                UIBarButtonItem(
                    title = action.title,
                    style = action.style.toUIBarButtonItemStyle(),
                    target = targets[targetIndex++],
                    action = NSSelectorFromString(INPUT_ACTION_SELECTOR),
                )
            }
        }

        val toolbar = UIToolbar()
        toolbar.sizeToFit()
        val hasExplicitFlexibleSpace = actions.any {
            it.style == InputActionStyle.FlexibleSpace
        }
        toolbar.setItems(
            if (!hasExplicitFlexibleSpace && buttons.size > 1) {
                buttons.dropLast(1) +
                    UIBarButtonItem(
                        barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
                        target = null,
                        action = null,
                    ) +
                    buttons.last()
            } else {
                buttons
            },
            animated = false,
        )

        toolbarTargets = targets
        currentToolbar = toolbar
        return toolbar
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun wrapToolbarForIosVersion(
    toolbar: UIToolbar,
    majorVersion: Int,
): UIView {
    if (majorVersion < IOS26_MAJOR_VERSION) {
        return toolbar
    }

    val contentHeight = toolbar.frame.useContents { size.height }
        .takeIf { it > 0.0 }
        ?: 44.0
    return IosToolbarAccessoryContainer(
        toolbar = toolbar,
        contentHeight = contentHeight,
        bottomGap = IOS26_TOOLBAR_BOTTOM_GAP,
    )
}

@OptIn(ExperimentalForeignApi::class)
internal class IosToolbarAccessoryContainer(
    private val toolbar: UIToolbar,
    private val contentHeight: Double,
    internal val bottomGap: Double,
) : UIInputView(
    frame = CGRectMake(0.0, 0.0, 0.0, contentHeight + bottomGap),
    inputViewStyle = UIInputViewStyle.UIInputViewStyleDefault,
) {
    internal val reservedHeight: Double = contentHeight + bottomGap

    init {
        allowsSelfSizing = true
        translatesAutoresizingMaskIntoConstraints = false
        backgroundColor = null
        addSubview(toolbar)
        toolbar.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activateConstraints(
            listOf(
                toolbar.topAnchor.constraintEqualToAnchor(topAnchor),
                toolbar.leadingAnchor.constraintEqualToAnchor(leadingAnchor),
                toolbar.trailingAnchor.constraintEqualToAnchor(trailingAnchor),
                toolbar.heightAnchor.constraintEqualToConstant(contentHeight),
                heightAnchor.constraintEqualToConstant(reservedHeight),
            ),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun InputActionStyle.toUIBarButtonItemStyle(): UIBarButtonItemStyle {
    return when (this) {
        InputActionStyle.Plain -> UIBarButtonItemStyle.UIBarButtonItemStylePlain
        InputActionStyle.Done -> UIBarButtonItemStyle.UIBarButtonItemStyleDone
        InputActionStyle.FlexibleSpace -> error("FlexibleSpace does not have a button style")
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosInputActionTarget(
    private val onClick: () -> Unit,
) : NSObject() {
    @ObjCAction
    fun invoke(sender: UIBarButtonItem) {
        onClick()
    }
}

internal actual fun createInputActionsHost(): InputActionHost {
    return IosInputActionsHost()
}
