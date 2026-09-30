package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable

object StringsGamesYearguessCasualties {
    val yearguess_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Guess the Year"
            AppLanguage.ES -> "Adivina el Año"
            AppLanguage.FR -> "Devine l'Année"
            AppLanguage.DE -> "Errate das Jahr"
            AppLanguage.IT -> "Indovina l'Anno"
        }

    val yearguess_back_cd: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back"
            AppLanguage.ES -> "Atrás"
            AppLanguage.FR -> "Retour"
            AppLanguage.DE -> "Zurück"
            AppLanguage.IT -> "Indietro"
        }

    @Composable
    fun yearguess_question_counter(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Question %1\$d / %2\$d"
            AppLanguage.ES -> "Pregunta %1\$d / %2\$d"
            AppLanguage.FR -> "Question %1\$d / %2\$d"
            AppLanguage.DE -> "Frage %1\$d / %2\$d"
            AppLanguage.IT -> "Domanda %1\$d / %2\$d"
        }
        return formatTemplate(template, arg1, arg2)
    }

    @Composable
    fun yearguess_hint_prefix(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Hint: %1\$s"
            AppLanguage.ES -> "Pista: %1\$s"
            AppLanguage.FR -> "Indice : %1\$s"
            AppLanguage.DE -> "Hinweis: %1\$s"
            AppLanguage.IT -> "Suggerimento: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    val yearguess_hint_unavailable: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "No hint available for this one."
            AppLanguage.ES -> "No hay pista disponible para este evento."
            AppLanguage.FR -> "Aucun indice disponible pour cet événement."
            AppLanguage.DE -> "Für dieses Ereignis ist kein Hinweis verfügbar."
            AppLanguage.IT -> "Nessun suggerimento disponibile per questo evento."
        }

    val yearguess_guess_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Your guess (year)"
            AppLanguage.ES -> "Tu respuesta (año)"
            AppLanguage.FR -> "Ta réponse (année)"
            AppLanguage.DE -> "Deine Vermutung (Jahr)"
            AppLanguage.IT -> "La tua risposta (anno)"
        }

    val yearguess_submit: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Submit"
            AppLanguage.ES -> "Enviar"
            AppLanguage.FR -> "Valider"
            AppLanguage.DE -> "Bestätigen"
            AppLanguage.IT -> "Invia"
        }

    val yearguess_see_results: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "See Results"
            AppLanguage.ES -> "Ver Resultados"
            AppLanguage.FR -> "Voir les Résultats"
            AppLanguage.DE -> "Ergebnisse ansehen"
            AppLanguage.IT -> "Vedi Risultati"
        }

    val yearguess_next_question: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Next Question"
            AppLanguage.ES -> "Siguiente Pregunta"
            AppLanguage.FR -> "Question Suivante"
            AppLanguage.DE -> "Nächste Frage"
            AppLanguage.IT -> "Domanda Successiva"
        }

    val yearguess_era_ad: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "AD"
            AppLanguage.ES -> "d.C."
            AppLanguage.FR -> "apr. J.-C."
            AppLanguage.DE -> "n. Chr."
            AppLanguage.IT -> "d.C."
        }

    val yearguess_era_bce: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "BCE"
            AppLanguage.ES -> "a.C."
            AppLanguage.FR -> "av. J.-C."
            AppLanguage.DE -> "v. Chr."
            AppLanguage.IT -> "a.C."
        }

    @Composable
    fun yearguess_landed_too_high(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s yrs too high"
            AppLanguage.ES -> "%1\$s años demasiado alto"
            AppLanguage.FR -> "%1\$s ans trop tard"
            AppLanguage.DE -> "%1\$s Jahre zu spät"
            AppLanguage.IT -> "%1\$s anni troppo avanti"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun yearguess_landed_too_low(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s yrs too low"
            AppLanguage.ES -> "%1\$s años demasiado bajo"
            AppLanguage.FR -> "%1\$s ans trop tôt"
            AppLanguage.DE -> "%1\$s Jahre zu früh"
            AppLanguage.IT -> "%1\$s anni troppo indietro"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun yearguess_points_badge(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s pts"
            AppLanguage.ES -> "%1\$s pts"
            AppLanguage.FR -> "%1\$s pts"
            AppLanguage.DE -> "%1\$s Pkt."
            AppLanguage.IT -> "%1\$s pt"
        }
        return formatTemplate(template, arg1)
    }

    val yearguess_back_to_menu: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back to Menu"
            AppLanguage.ES -> "Volver al Menú"
            AppLanguage.FR -> "Retour au Menu"
            AppLanguage.DE -> "Zurück zum Menü"
            AppLanguage.IT -> "Torna al Menu"
        }

    val casualties_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Casualty Count"
            AppLanguage.ES -> "Número de Bajas"
            AppLanguage.FR -> "Nombre de Pertes"
            AppLanguage.DE -> "Verlustzahlen"
            AppLanguage.IT -> "Numero di Vittime"
        }

    val casualties_back_cd: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back"
            AppLanguage.ES -> "Atrás"
            AppLanguage.FR -> "Retour"
            AppLanguage.DE -> "Zurück"
            AppLanguage.IT -> "Indietro"
        }

    @Composable
    fun casualties_question_counter(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Question %1\$d / %2\$d"
            AppLanguage.ES -> "Pregunta %1\$d / %2\$d"
            AppLanguage.FR -> "Question %1\$d / %2\$d"
            AppLanguage.DE -> "Frage %1\$d / %2\$d"
            AppLanguage.IT -> "Domanda %1\$d / %2\$d"
        }
        return formatTemplate(template, arg1, arg2)
    }

    @Composable
    fun casualties_hint_prefix(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Hint: %1\$s"
            AppLanguage.ES -> "Pista: %1\$s"
            AppLanguage.FR -> "Indice : %1\$s"
            AppLanguage.DE -> "Hinweis: %1\$s"
            AppLanguage.IT -> "Suggerimento: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    val casualties_hint_unavailable: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "No hint available for this one."
            AppLanguage.ES -> "No hay pista disponible para esta batalla."
            AppLanguage.FR -> "Aucun indice disponible pour cette bataille."
            AppLanguage.DE -> "Für diese Schlacht ist kein Hinweis verfügbar."
            AppLanguage.IT -> "Nessun suggerimento disponibile per questa battaglia."
        }

    val casualties_guess_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Your guess (casualties)"
            AppLanguage.ES -> "Tu respuesta (bajas)"
            AppLanguage.FR -> "Ta réponse (pertes)"
            AppLanguage.DE -> "Deine Vermutung (Verluste)"
            AppLanguage.IT -> "La tua risposta (vittime)"
        }

    val casualties_submit: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Submit"
            AppLanguage.ES -> "Enviar"
            AppLanguage.FR -> "Valider"
            AppLanguage.DE -> "Bestätigen"
            AppLanguage.IT -> "Invia"
        }

    val casualties_see_results: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "See Results"
            AppLanguage.ES -> "Ver Resultados"
            AppLanguage.FR -> "Voir les Résultats"
            AppLanguage.DE -> "Ergebnisse ansehen"
            AppLanguage.IT -> "Vedi Risultati"
        }

    val casualties_next_question: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Next Question"
            AppLanguage.ES -> "Siguiente Pregunta"
            AppLanguage.FR -> "Question Suivante"
            AppLanguage.DE -> "Nächste Frage"
            AppLanguage.IT -> "Domanda Successiva"
        }

    @Composable
    fun casualties_landed_too_high(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s too high"
            AppLanguage.ES -> "%1\$s demasiado alto"
            AppLanguage.FR -> "%1\$s trop élevé"
            AppLanguage.DE -> "%1\$s zu hoch"
            AppLanguage.IT -> "%1\$s troppo alto"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun casualties_landed_too_low(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s too low"
            AppLanguage.ES -> "%1\$s demasiado bajo"
            AppLanguage.FR -> "%1\$s trop bas"
            AppLanguage.DE -> "%1\$s zu niedrig"
            AppLanguage.IT -> "%1\$s troppo basso"
        }
        return formatTemplate(template, arg1)
    }

    val casualties_final_multiplier_caption: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Final multiplier (1.00x is a perfect run)"
            AppLanguage.ES -> "Multiplicador final (1.00x es una partida perfecta)"
            AppLanguage.FR -> "Multiplicateur final (1,00x est une partie parfaite)"
            AppLanguage.DE -> "Endmultiplikator (1,00x ist ein perfekter Lauf)"
            AppLanguage.IT -> "Moltiplicatore finale (1,00x è una partita perfetta)"
        }

    val casualties_back_to_menu: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back to Menu"
            AppLanguage.ES -> "Volver al Menú"
            AppLanguage.FR -> "Retour au Menu"
            AppLanguage.DE -> "Zurück zum Menü"
            AppLanguage.IT -> "Torna al Menu"
        }

}
