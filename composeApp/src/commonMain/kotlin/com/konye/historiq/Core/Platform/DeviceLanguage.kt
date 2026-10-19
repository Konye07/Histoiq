package com.konye.historiq.Core.Platform

/** The language codes the device is set to ("hu", "en", ...), most preferred first and without the region ("pt-BR" is "pt"). */
expect fun devicePreferredLanguages(): List<String>
