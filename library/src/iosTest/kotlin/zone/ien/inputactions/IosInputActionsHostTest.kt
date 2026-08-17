package zone.ien.inputactions

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
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
    fun toolbarIsAbsentWhenNoActionsAreActive() {
        val host = IosInputActionsHost()

        assertNull(host.createInputAccessoryView())
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
}
