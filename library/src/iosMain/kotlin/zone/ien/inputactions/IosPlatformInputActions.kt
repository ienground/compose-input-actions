package zone.ien.inputactions

// Compose 1.11.x exposes the iOS inputAccessoryView through the public platform text-input
// interception API. Keep this bridge isolated so future Compose changes remain localized here.

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.platform.PlatformTextInputSession
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.input.autocapitalizationType
import androidx.compose.ui.text.input.autocorrectionType
import androidx.compose.ui.text.input.enablesReturnKeyAutomatically
import androidx.compose.ui.text.input.inputAccessoryView
import androidx.compose.ui.text.input.inputView
import androidx.compose.ui.text.input.isSecureTextEntry
import androidx.compose.ui.text.input.keyboardAppearance
import androidx.compose.ui.text.input.keyboardType
import androidx.compose.ui.text.input.returnKeyType
import androidx.compose.ui.text.input.textContentType
import androidx.compose.ui.text.input.usingNativeTextInput
import androidx.compose.ui.text.input.writingToolsBehavior
import platform.UIKit.UIKeyboardAppearanceDefault
import platform.UIKit.UIView

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun ProvidePlatformInputActions(
    host: InputActionHost,
    content: @Composable () -> Unit,
) {
    val iosHost = host as? IosInputActionsHost
    if (iosHost == null) {
        content()
        return
    }

    val actionsVersion = iosHost.actionsVersion
    val interceptor = remember(iosHost, actionsVersion) {
        IosInputActionsInterceptor(iosHost)
    }

    InterceptPlatformTextInput(
        interceptor = interceptor,
        content = content,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private class IosInputActionsInterceptor(
    private val host: IosInputActionsHost,
) : PlatformTextInputInterceptor {
    override suspend fun interceptStartInputMethod(
        request: PlatformTextInputMethodRequest,
        nextHandler: PlatformTextInputSession,
    ): Nothing {
        val toolbar = host.createInputAccessoryView()
        val nextRequest = toolbar?.let { request.withInputAccessoryView(it) } ?: request
        nextHandler.startInputMethod(nextRequest)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun PlatformTextInputMethodRequest.withInputAccessoryView(
    toolbar: UIView,
): PlatformTextInputMethodRequest {
    val currentPlatformOptions = imeOptions.platformImeOptions
    val updatedImeOptions = imeOptions.copy(
        platformImeOptions = PlatformImeOptions {
            currentPlatformOptions?.keyboardType?.let(::keyboardType)
            keyboardAppearance(currentPlatformOptions?.keyboardAppearance ?: UIKeyboardAppearanceDefault)
            currentPlatformOptions?.returnKeyType?.let(::returnKeyType)
            currentPlatformOptions?.textContentType?.let(::textContentType)
            currentPlatformOptions?.isSecureTextEntry?.let(::isSecureTextEntry)
            currentPlatformOptions?.enablesReturnKeyAutomatically?.let(::enablesReturnKeyAutomatically)
            currentPlatformOptions?.autocapitalizationType?.let(::autocapitalizationType)
            currentPlatformOptions?.autocorrectionType?.let(::autocorrectionType)
            currentPlatformOptions?.inputView?.let(::inputView)
            inputAccessoryView(toolbar)
            currentPlatformOptions?.writingToolsBehavior?.let(::writingToolsBehavior)
            currentPlatformOptions?.usingNativeTextInput?.let(::usingNativeTextInput)
        },
    )

    return IosInputActionsTextInputRequest(this, updatedImeOptions)
}

@OptIn(ExperimentalComposeUiApi::class)
private class IosInputActionsTextInputRequest(
    private val delegate: PlatformTextInputMethodRequest,
    override val imeOptions: ImeOptions,
) : PlatformTextInputMethodRequest by delegate
