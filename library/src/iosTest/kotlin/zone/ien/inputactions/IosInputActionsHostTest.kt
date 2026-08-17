package zone.ien.inputactions

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIToolbar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosInputActionsHostTest {
    @Test
    fun toolbarPlacesFlexibleSpaceBeforeTheFinalAction() {
        val host = IosInputActionsHost()
        val target = InputActionTarget()

        host.registerActions(
            target,
            listOf(
                InputAction(title = "button", onClick = {}),
                InputAction(
                    title = "Done",
                    onClick = {},
                    style = InputActionStyle.Done,
                ),
            ),
        )

        val items = assertNotNull(host.createToolbar()).items
            .orEmpty()
            .map { it as UIBarButtonItem }

        assertEquals(3, items.size)
        assertEquals("button", items[0].title)
        assertEquals(UIBarButtonItemStyle.UIBarButtonItemStylePlain, items[0].style)
        assertNull(items[1].title)
        assertEquals("Done", items[2].title)
        assertEquals(UIBarButtonItemStyle.UIBarButtonItemStyleDone, items[2].style)
    }

    @Test
    fun toolbarUsesAnExplicitFlexibleSpaceWithoutAddingAnotherOne() {
        val host = IosInputActionsHost()
        val target = InputActionTarget()

        host.registerActions(
            target,
            listOf(
                InputAction(title = "Previous", onClick = {}),
                InputAction.FlexibleSpace,
                InputAction(
                    title = "Done",
                    style = InputActionStyle.Done,
                    onClick = {},
                ),
            ),
        )

        val items = assertNotNull(host.createToolbar()).items
            .orEmpty()
            .map { it as UIBarButtonItem }

        assertEquals(3, items.size)
        assertEquals("Previous", items[0].title)
        assertNull(items[1].title)
        assertEquals("Done", items[2].title)
    }

    @Test
    fun pillStyleUsesTheSystemToolbar() {
        val host = IosInputActionsHost()
        val target = InputActionTarget()

        host.registerActions(
            target,
            listOf(InputAction(title = "Done", onClick = {})),
            style = InputActionsStyle.Pill,
        )

        assertNotNull(host.createToolbar())
    }

    @Test
    fun pillStyleKeepsDoneActionInTheSharedBackgroundGroup() {
        val host = IosInputActionsHost()
        val target = InputActionTarget()

        host.registerActions(
            target,
            listOf(
                InputAction(title = "Previous", onClick = {}),
                InputAction(
                    title = "Done",
                    style = InputActionStyle.Done,
                    onClick = {},
                ),
            ),
            style = InputActionsStyle.Pill,
        )

        val items = assertNotNull(host.createToolbar()).items
            .orEmpty()
            .map { it as UIBarButtonItem }

        assertEquals(UIBarButtonItemStyle.UIBarButtonItemStylePlain, items.last().style)
        val majorVersion = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
            majorVersion.toInt()
        }
        if (majorVersion >= 26) {
            assertTrue(items.all { it.sharesBackground })
        }
    }

    @Test
    fun toolbarUsesAnSfSymbolForAnIconAction() {
        val host = IosInputActionsHost()
        val target = InputActionTarget()

        host.registerActions(
            target,
            listOf(
                InputAction(
                    title = "Previous",
                    icon = InputActionIcon(systemName = "chevron.up"),
                    onClick = {},
                ),
            ),
        )

        val item = assertNotNull(host.createToolbar()).items
            .orEmpty()
            .map { it as UIBarButtonItem }
            .single()

        assertNotNull(item.image)
        assertEquals("Previous", item.title)
    }

    @Test
    fun updatingActionsRefreshesTheExistingToolbarItems() {
        val host = IosInputActionsHost()
        val target = InputActionTarget()

        host.registerActions(
            target,
            listOf(InputAction(title = "First", onClick = {})),
        )
        val toolbar = assertNotNull(host.createToolbar())

        host.registerActions(
            target,
            listOf(InputAction(title = "Second", onClick = {})),
        )

        assertSame(toolbar, host.createToolbar())
        val items = toolbar.items
            .orEmpty()
            .map { it as UIBarButtonItem }
        assertEquals("Second", items.singleOrNull()?.title)
    }

    @Test
    fun ios26ToolbarIsWrappedButOlderVersionsKeepTheToolbarView() {
        val toolbar = UIToolbar()

        val ios26Accessory = wrapToolbarForIosVersion(toolbar, 26)
        val ios25Accessory = wrapToolbarForIosVersion(toolbar, 25)

        assertTrue(ios26Accessory is IosToolbarAccessoryContainer)
        assertSame(toolbar, ios25Accessory)
    }

    @Test
    fun toolbarContainerReservesTheBottomGap() {
        val container = IosToolbarAccessoryContainer(
            toolbar = UIToolbar(),
            contentHeight = 44.0,
            bottomGap = 8.0,
        )

        assertTrue(container.allowsSelfSizing)
        assertEquals(52.0, container.reservedHeight, absoluteTolerance = 0.01)
    }

    @Test
    fun toolbarIsAbsentWhenNoActionsAreActive() {
        val host = IosInputActionsHost()

        assertNull(host.createInputAccessoryView())
    }

}
