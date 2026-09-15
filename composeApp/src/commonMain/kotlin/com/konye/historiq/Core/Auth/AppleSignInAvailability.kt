package com.konye.historiq.Core.Auth

/**
 * Whether to show the "Continue with Apple" button at all. False on Android: Apple sign-in only
 * ever makes sense on Apple's own platforms, and showing a mock (non-functional-as-branded)
 * "Continue with Apple" button to real users/reviewers is a real risk in its own right, separate
 * from the general mock-sign-in disclosure — see the privacy policy's "placeholder sign-in"
 * section. True on iOS, where it's still the mock-now stub until real Sign in with Apple exists.
 */
expect val showAppleSignInButton: Boolean
