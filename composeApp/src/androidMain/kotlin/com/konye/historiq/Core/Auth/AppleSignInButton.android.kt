package com.konye.historiq.Core.Auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** Android never shows the button (see [showAppleSignInButton]), so there is nothing to draw. */
@Composable
actual fun AppleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean,
    darkTheme: Boolean,
    height: Dp,
    modifier: Modifier
) = Unit
