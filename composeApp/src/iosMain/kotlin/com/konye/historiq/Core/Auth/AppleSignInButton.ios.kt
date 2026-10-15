package com.konye.historiq.Core.Auth

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.UIKitView
import platform.AuthenticationServices.ASAuthorizationAppleIDButton
import platform.AuthenticationServices.ASAuthorizationAppleIDButtonStyle
import platform.AuthenticationServices.ASAuthorizationAppleIDButtonTypeContinue
import platform.UIKit.UIAction
import platform.UIKit.UIControlEventTouchUpInside

@Composable
actual fun AppleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean,
    darkTheme: Boolean,
    height: Dp,
    modifier: Modifier
) {
    val currentOnClick by rememberUpdatedState(onClick)
    // A button's style is fixed when it is created, so a change of theme builds a new one.
    key(darkTheme) {
        UIKitView(
            factory = {
                val style = if (darkTheme) {
                    ASAuthorizationAppleIDButtonStyle.ASAuthorizationAppleIDButtonStyleWhite
                } else {
                    ASAuthorizationAppleIDButtonStyle.ASAuthorizationAppleIDButtonStyleBlack
                }
                ASAuthorizationAppleIDButton(ASAuthorizationAppleIDButtonTypeContinue, style).apply {
                    cornerRadius = height.value / 2.0
                    addAction(UIAction.actionWithHandler { currentOnClick() }, UIControlEventTouchUpInside)
                }
            },
            modifier = modifier.height(height),
            update = { button ->
                button.userInteractionEnabled = enabled
                button.alpha = if (enabled) 1.0 else 0.5
            }
        )
    }
}
