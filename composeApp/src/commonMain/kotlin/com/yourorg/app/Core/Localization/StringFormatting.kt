package com.yourorg.app.Core.Localization

private val PLACEHOLDER = Regex("""%(\d+)\$[sd]""")

/** Substitutes Android-style positional placeholders (`%1$s`, `%2$d`, ...) in [template]. */
fun formatTemplate(template: String, vararg args: Any): String =
    PLACEHOLDER.replace(template) { match ->
        val index = match.groupValues[1].toInt() - 1
        args.getOrNull(index)?.toString() ?: match.value
    }
