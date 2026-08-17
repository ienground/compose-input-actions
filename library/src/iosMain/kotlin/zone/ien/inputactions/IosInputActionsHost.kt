package zone.ien.inputactions

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
import platform.UIKit.UIImage
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
    private var toolbarTargets: List<IosInputActionTarget> = emptyList()
    private var currentToolbar: UIToolbar? = null

    internal fun registerActions(target: InputActionTarget, actions: List<InputAction>) {
        registerActions(target, actions, InputActionsStyle.Toolbar)
    }

    override fun registerActions(
        target: InputActionTarget,
        actions: List<InputAction>,
        style: InputActionsStyle,
    ) {
        super.registerActions(target, actions, style)
        val toolbar = currentToolbar
        if (toolbar == null) {
            toolbarTargets = emptyList()
        } else if (registry.activeActions().isEmpty()) {
            toolbar.setItems(emptyList<UIBarButtonItem>(), animated = false)
            currentToolbar = null
            toolbarTargets = emptyList()
        } else {
            updateToolbarItems(toolbar, registry.activeActions(), registry.activeStyle())
        }
    }

    override fun unregisterActions(target: InputActionTarget) {
        super.unregisterActions(target)
        val toolbar = currentToolbar
        val actions = registry.activeActions()
        if (toolbar == null || actions.isEmpty()) {
            currentToolbar = null
            toolbarTargets = emptyList()
        } else {
            updateToolbarItems(toolbar, actions, registry.activeStyle())
        }
    }

    override fun dispose() {
        currentToolbar = null
        toolbarTargets = emptyList()
        super.dispose()
    }

    internal fun createInputAccessoryView(): UIView? {
        val toolbar = createToolbar() ?: return null
        val majorVersion = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
            majorVersion.toInt()
        }
        return when (registry.activeStyle()) {
            // UIKit owns the visual treatment for both styles. On iOS 26 this allows the
            // system toolbar appearance to provide its native Liquid Glass presentation.
            InputActionsStyle.Toolbar,
            InputActionsStyle.Pill -> wrapToolbarForIosVersion(toolbar, majorVersion)
        }
    }

    internal fun createToolbar(): UIToolbar? {
        val actions = registry.activeActions()
        if (actions.isEmpty()) {
            currentToolbar = null
            toolbarTargets = emptyList()
            return null
        }

        val toolbar = currentToolbar ?: UIToolbar().also {
            it.sizeToFit()
            currentToolbar = it
        }
        updateToolbarItems(toolbar, actions, registry.activeStyle())
        return toolbar
    }

    private fun updateToolbarItems(
        toolbar: UIToolbar,
        actions: List<InputAction>,
        presentationStyle: InputActionsStyle,
    ) {
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
                createBarButtonItem(
                    action = action,
                    target = targets[targetIndex++],
                    presentationStyle = presentationStyle,
                ).also { item ->
                    item.configureSharedBackground(presentationStyle)
                }
            }
        }
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
    }

    private fun createBarButtonItem(
        action: InputAction,
        target: IosInputActionTarget,
        presentationStyle: InputActionsStyle,
    ): UIBarButtonItem {
        val style = action.style.toUIBarButtonItemStyle(presentationStyle)
        val image = action.icon?.let { UIImage.systemImageNamed(it.systemName) }
        val item = if (image != null) {
            UIBarButtonItem(
                image = image,
                style = style,
                target = target,
                action = NSSelectorFromString(INPUT_ACTION_SELECTOR),
            )
        } else {
            UIBarButtonItem(
                title = action.title,
                style = style,
                target = target,
                action = NSSelectorFromString(INPUT_ACTION_SELECTOR),
            )
        }
        if (image != null && action.title.isNotBlank()) {
            // UIBarButtonItem has no accessibilityLabel property; retain the human-readable
            // title on the item so UIKit can expose it while rendering the SF Symbol.
            item.title = action.title
        }
        return item
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun UIBarButtonItem.configureSharedBackground(
    presentationStyle: InputActionsStyle,
) {
    if (presentationStyle != InputActionsStyle.Pill) {
        return
    }

    val majorVersion = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
        majorVersion.toInt()
    }
    if (majorVersion >= IOS26_MAJOR_VERSION) {
        sharesBackground = true
        hidesSharedBackground = false
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

/**
 * Reserves the iOS 26 keyboard-to-toolbar gap without changing the toolbar's appearance.
 *
 * The container is intentionally transparent and only participates in layout. The nested
 * [UIToolbar] remains the native accessory view that owns its system appearance.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosToolbarAccessoryContainer(
    toolbar: UIToolbar,
    contentHeight: Double,
    internal val bottomGap: Double,
) : UIInputView(
    frame = CGRectMake(0.0, 0.0, 0.0, contentHeight + bottomGap),
    inputViewStyle = UIInputViewStyle.UIInputViewStyleDefault,
) {
    internal val reservedHeight: Double = contentHeight + bottomGap

    init {
        // Compose's responder presents inputAccessoryView flush to the iOS 26 keyboard.
        // Keep this layout-only wrapper isolated so the native UIToolbar appearance remains
        // fully controlled by UIKit.
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
private fun InputActionStyle.toUIBarButtonItemStyle(
    presentationStyle: InputActionsStyle,
): UIBarButtonItemStyle {
    return when (this) {
        InputActionStyle.Plain -> UIBarButtonItemStyle.UIBarButtonItemStylePlain
        InputActionStyle.Done -> if (presentationStyle == InputActionsStyle.Pill) {
            // iOS 26 aliases .done to .prominent, which intentionally breaks the shared
            // Liquid Glass background. Pill keeps the completion action in the same group.
            UIBarButtonItemStyle.UIBarButtonItemStylePlain
        } else {
            UIBarButtonItemStyle.UIBarButtonItemStyleDone
        }
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
