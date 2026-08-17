package zone.ien.inputactions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InputActionTest {
    @Test
    fun testInputActionInitialization() {
        var clicked = false
        val action = InputAction(title = "Done", onClick = { clicked = true })

        assertEquals("Done", action.title)
        assertEquals(InputActionStyle.Plain, action.style)
        action.onClick()
        assertTrue(clicked)
    }

    @Test
    fun inputActionCanUseDoneStyle() {
        val action = InputAction(
            title = "Done",
            style = InputActionStyle.Done,
            onClick = {},
        )

        assertEquals(InputActionStyle.Done, action.style)
    }

    @Test
    fun flexibleSpaceActionIsAvailableFromInputAction() {
        val action = InputAction.FlexibleSpace

        assertEquals("", action.title)
        assertEquals(InputActionStyle.FlexibleSpace, action.style)
        action.onClick()
    }

    @Test
    fun registeringTargetMakesItsActionsActive() {
        val registry = InputActionRegistry()
        val target = InputActionTarget()
        val action = InputAction(title = "Done", onClick = {})

        registry.register(target, listOf(action))

        assertEquals(listOf(action), registry.activeActions())
    }

    @Test
    fun registeringAnotherTargetSwitchesActiveActions() {
        val registry = InputActionRegistry()
        val firstTarget = InputActionTarget()
        val secondTarget = InputActionTarget()
        val firstAction = InputAction(title = "Next", onClick = {})
        val secondAction = InputAction(title = "Done", onClick = {})

        registry.register(firstTarget, listOf(firstAction))
        registry.register(secondTarget, listOf(secondAction))

        assertEquals(listOf(secondAction), registry.activeActions())
    }

    @Test
    fun unregisteringInactiveTargetDoesNotClearActiveActions() {
        val registry = InputActionRegistry()
        val firstTarget = InputActionTarget()
        val secondTarget = InputActionTarget()
        val secondAction = InputAction(title = "Done", onClick = {})

        registry.register(firstTarget, listOf(InputAction(title = "Next", onClick = {})))
        registry.register(secondTarget, listOf(secondAction))
        registry.unregister(firstTarget)

        assertEquals(listOf(secondAction), registry.activeActions())
    }

    @Test
    fun updatingActiveTargetReplacesItsActions() {
        val registry = InputActionRegistry()
        val target = InputActionTarget()
        val originalAction = InputAction(title = "Next", onClick = {})
        val replacementAction = InputAction(title = "Done", onClick = {})

        registry.register(target, listOf(originalAction))
        registry.register(target, listOf(replacementAction))

        assertEquals(listOf(replacementAction), registry.activeActions())
    }

    @Test
    fun unregisteringActiveTargetClearsActions() {
        val registry = InputActionRegistry()
        val target = InputActionTarget()

        registry.register(target, listOf(InputAction(title = "Done", onClick = {})))
        registry.unregister(target)

        assertTrue(registry.activeActions().isEmpty())
    }
}
