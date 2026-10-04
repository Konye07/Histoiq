package com.konye.historiq

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

// Keyboard avoidance is done with imePadding(); the default FocusableAboveKeyboard pan would over-shift on top of it.
fun MainViewController(): UIViewController =
    ComposeUIViewController(configure = { onFocusBehavior = OnFocusBehavior.DoNothing }) { App() }
