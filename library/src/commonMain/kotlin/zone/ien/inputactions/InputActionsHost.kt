package zone.ien.inputactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Internal host used by the Modifier API to keep the active action set.
 */
internal interface InputActionHost {
    fun registerActions(target: InputActionTarget, actions: List<InputAction>)

    fun unregisterActions(target: InputActionTarget)

    fun dispose()
}

internal val LocalInputActionsHost = staticCompositionLocalOf<InputActionHost?> { null }

internal expect fun createInputActionsHost(): InputActionHost

@Composable
internal expect fun ProvidePlatformInputActions(
    host: InputActionHost,
    content: @Composable () -> Unit,
)

/**
 * Provides the input-action host used by [Modifier.inputActions] in the content subtree.
 *
 * Place one host around the part of the UI that contains text fields using input actions. On iOS,
 * the host presents the active actions in the keyboard's native input accessory toolbar.
 */
@Composable
public fun InputActionsHost(
    content: @Composable () -> Unit,
) {
    val host = remember { createInputActionsHost() }

    DisposableEffect(host) {
        onDispose(host::dispose)
    }

    ProvidePlatformInputActions(host) {
        CompositionLocalProvider(
            LocalInputActionsHost provides host,
            content = content,
        )
    }
}
