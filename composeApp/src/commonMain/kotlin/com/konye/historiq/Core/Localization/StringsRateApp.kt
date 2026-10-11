package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable

object StringsRateApp {
    val rate_app_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Enjoying Historiq?"
            AppLanguage.ES -> "¿Te Gusta Historiq?"
            AppLanguage.FR -> "Tu Aimes Historiq ?"
            AppLanguage.DE -> "Gefällt Dir Historiq?"
            AppLanguage.IT -> "Ti Piace Historiq?"
            AppLanguage.HU -> "Tetszik a Historiq?"
        }

    val rate_app_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "If you like the app, a quick rating on the Play Store really helps."
            AppLanguage.ES -> "Si te gusta la app, una valoración rápida en Play Store ayuda mucho."
            AppLanguage.FR -> "Si tu aimes l'appli, une petite note sur le Play Store aide beaucoup."
            AppLanguage.DE -> "Wenn dir die App gefällt, hilft eine kurze Bewertung im Play Store sehr."
            AppLanguage.IT -> "Se ti piace l'app, una valutazione veloce sul Play Store aiuta molto."
            AppLanguage.HU -> "Ha tetszik az alkalmazás, egy gyors értékelés a Play Áruházban sokat segít."
        }

    val rate_app_rate_now: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Rate Now"
            AppLanguage.ES -> "Valorar Ahora"
            AppLanguage.FR -> "Noter Maintenant"
            AppLanguage.DE -> "Jetzt Bewerten"
            AppLanguage.IT -> "Valuta Ora"
            AppLanguage.HU -> "Értékelem"
        }

    val rate_app_later: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Maybe Later"
            AppLanguage.ES -> "Quizás Luego"
            AppLanguage.FR -> "Plus Tard"
            AppLanguage.DE -> "Vielleicht Später"
            AppLanguage.IT -> "Più Tardi"
            AppLanguage.HU -> "Talán később"
        }

    val rate_app_never: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "No Thanks"
            AppLanguage.ES -> "No, Gracias"
            AppLanguage.FR -> "Non Merci"
            AppLanguage.DE -> "Nein Danke"
            AppLanguage.IT -> "No Grazie"
            AppLanguage.HU -> "Nem, köszönöm"
        }
}
