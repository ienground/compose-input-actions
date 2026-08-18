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

    override fun registerActions(target: InputActionTarget, actions: List<InputAction>) {
        super.registerActions(target, actions)
        val toolbar = currentToolbar
        if (toolbar == null) {
            toolbarTargets = emptyList()
        } else if (registry.activeActions().isEmpty()) {
            toolbar.setItems(emptyList<UIBarButtonItem>(), animated = false)
            currentToolbar = null
            toolbarTargets = emptyList()
        } else {
            updateToolbarItems(toolbar, registry.activeActions())
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
            updateToolbarItems(toolbar, actions)
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
        return wrapToolbarForIosVersion(toolbar, majorVersion)
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
        updateToolbarItems(toolbar, actions)
        return toolbar
    }

    private fun updateToolbarItems(
        toolbar: UIToolbar,
        actions: List<InputAction>,
    ) {
        val targets = actions
            .filterNot { it.isFlexibleSpace }
            .map { action -> IosInputActionTarget(action.onClick) }
        var targetIndex = 0
        val itemGroups = actions.map { action ->
            val separator = if (action.separatesSharedBackground) {
                createFixedSpaceItem()
            } else {
                null
            }
            val item = if (action.isFlexibleSpace) {
                createFlexibleSpaceItem(action.hidesSharedBackground)
            } else {
                createBarButtonItem(
                    action = action,
                    target = targets[targetIndex++],
                )
            }
            if (action.isFlexibleSpace) {
                listOf(item) + listOfNotNull(separator)
            } else {
                listOfNotNull(separator) + item
            }
        }
        val hasExplicitFlexibleSpace = actions.any {
            it.isFlexibleSpace
        }
        val items = itemGroups.flatten().toMutableList()
        if (!hasExplicitFlexibleSpace && actions.size > 1) {
            val finalGroupStart = itemGroups
                .dropLast(1)
                .sumOf { it.size }
            items.add(finalGroupStart, createFlexibleSpaceItem())
        }
        toolbar.setItems(items, animated = false)
        toolbarTargets = targets
    }

    private fun createFixedSpaceItem(): UIBarButtonItem? {
        val majorVersion = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
            majorVersion.toInt()
        }
        if (majorVersion < IOS26_MAJOR_VERSION) {
            return null
        }
        return UIBarButtonItem.fixedSpaceItem()
    }

    private fun createFlexibleSpaceItem(
        hidesSharedBackground: Boolean = false,
    ): UIBarButtonItem {
        return UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
            target = null,
            action = null,
        ).also { item ->
            item.configureSharedBackground(hidesSharedBackground)
        }
    }

    private fun createBarButtonItem(
        action: InputAction,
        target: IosInputActionTarget,
    ): UIBarButtonItem {
        val style = action.style.toUIBarButtonItemStyle()
        val image = action.icon?.let { UIImage.systemImageNamed(it.systemName) }
        val item = (if (image != null) {
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
        }).also { item ->
            item.configureSharedBackground(action.hidesSharedBackground)
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
    hidesSharedBackground: Boolean,
) {
    val majorVersion = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
        majorVersion.toInt()
    }
    if (majorVersion >= IOS26_MAJOR_VERSION) {
        sharesBackground = !hidesSharedBackground
        this.hidesSharedBackground = hidesSharedBackground
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
private fun InputActionStyle.toUIBarButtonItemStyle(): UIBarButtonItemStyle {
    return when (this) {
        InputActionStyle.Plain -> UIBarButtonItemStyle.UIBarButtonItemStylePlain
        InputActionStyle.Done -> UIBarButtonItemStyle.UIBarButtonItemStyleDone
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
