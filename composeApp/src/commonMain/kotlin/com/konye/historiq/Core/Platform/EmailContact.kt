package com.konye.historiq.Core.Platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.konye.historiq.Core.Haptics.Haptics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** What the last tap on a contact-email control did. */
enum class EmailFeedback {
    /** The player asked to copy the address. */
    COPIED,

    /** The player asked to write an email, there was no mail app to do it in, so the address was copied instead. */
    NO_MAIL_APP
}

private const val FEEDBACK_MS = 2_500L

/**
 * Reaching Historiq by email from a control on screen. [open] takes the player to their mail app and, when the
 * phone has none (or none set up), copies the address instead so it can still be pasted anywhere; [copy] just
 * copies it. [feedback] says which of those just happened, for a few seconds, so the control can tell the player.
 */
@Stable
class EmailContact internal constructor(private val scope: CoroutineScope) {
    var feedback by mutableStateOf<EmailFeedback?>(null)
        private set

    private var clearing: Job? = null

    fun open(email: String) {
        openEmailComposer(email, onUnavailable = { copyAndReport(email, EmailFeedback.NO_MAIL_APP) })
    }

    fun copy(email: String) {
        Haptics.tick()
        copyAndReport(email, EmailFeedback.COPIED)
    }

    private fun copyAndReport(email: String, kind: EmailFeedback) {
        clearing?.cancel()
        clearing = scope.launch {
            try {
                copyToClipboard(email)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Some devices refuse clipboard writes (a managed work profile); say nothing rather than claim a copy.
                println("Historiq: could not write the clipboard — ${e::class.simpleName}: ${e.message}")
                return@launch
            }
            feedback = kind
            delay(FEEDBACK_MS)
            feedback = null
        }
    }
}

@Composable
fun rememberEmailContact(): EmailContact {
    val scope = rememberCoroutineScope()
    return remember(scope) { EmailContact(scope) }
}
