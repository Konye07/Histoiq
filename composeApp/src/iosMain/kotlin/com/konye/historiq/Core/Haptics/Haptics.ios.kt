package com.konye.historiq.Core.Haptics

import com.konye.historiq.Data.Settings.SettingsStore
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

actual object Haptics {
    actual fun tick() = feedback { UISelectionFeedbackGenerator().selectionChanged() }

    actual fun correct() = feedback {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }

    actual fun wrong() = feedback {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
    }

    actual fun perfect() = feedback {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy).impactOccurred()
    }

    actual fun scoreReveal() = feedback {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
    }

    private inline fun feedback(block: () -> Unit) {
        if (!SettingsStore.hapticsEnabled()) return
        block()
    }
}
