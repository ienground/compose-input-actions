package zone.ien.inputactions

import androidx.compose.runtime.Composable

internal actual fun createInputActionsHost(): InputActionHost {
    return CommonInputActionsHost()
}

@Composable
internal actual fun ProvidePlatformInputActions(
    host: InputActionHost,
    content: @Composable () -> Unit,
) {
    content()
}
