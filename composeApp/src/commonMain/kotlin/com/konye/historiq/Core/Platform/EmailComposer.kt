package com.konye.historiq.Core.Platform

/** The one inbox this app ever asks a player to write to — Privacy Policy contact and the Play-tab suggestion panel both point here. */
const val HISTORIQ_CONTACT_EMAIL = "historiq.help@gmail.com"

/**
 * Opens the platform's email composer pre-addressed to [email] (e.g. the Privacy Policy contact link).
 * [onUnavailable] is called when it could not be opened — a phone with no mail app, or none set up — so the
 * caller can offer the address some other way; see [EmailContact], which copies it.
 */
expect fun openEmailComposer(email: String, onUnavailable: () -> Unit = {})
