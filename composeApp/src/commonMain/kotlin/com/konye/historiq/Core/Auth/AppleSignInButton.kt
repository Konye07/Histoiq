package com.konye.historiq.Core.Auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * The "Continue with Apple" button. On iOS it is Apple's own `ASAuthorizationAppleIDButton`: App Review rejected the
 * app (Guideline 4, build 7) for a Material button with its own text, since Sign in with Apple must be drawn with
 * Apple's artwork and layout, which only the system button or Apple's downloadable resources provide. Black on a
 * light [darkTheme], white on a dark one, as Apple's guidelines say. Never shown on Android ([showAppleSignInButton]).
 */
@Composable
expect fun AppleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean,
    darkTheme: Boolean,
    height: Dp,
    modifier: Modifier = Modifier
)
