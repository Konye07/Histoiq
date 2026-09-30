package com.konye.historiq.Core.Platform

/** The one inbox this app ever asks a player to write to — Privacy Policy contact and the Play-tab suggestion panel both point here. */
const val HISTORIQ_CONTACT_EMAIL = "historiq.help@gmail.com"

/** Opens the platform's email composer pre-addressed to [email] (e.g. the Privacy Policy contact link). */
expect fun openEmailComposer(email: String)
