package com.yourorg.app.Core.Localization

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

    @Composable
    fun yearguess_correct(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Correct: %1\$s"
            AppLanguage.ES -> "Correcto: %1\$s"
            AppLanguage.FR -> "Correct : %1\$s"
            AppLanguage.DE -> "Richtig: %1\$s"
            AppLanguage.IT -> "Corretto: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun yearguess_score_points(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You scored %1\$s points"
            AppLanguage.ES -> "Obtuviste %1\$s puntos"
            AppLanguage.FR -> "Tu as marqué %1\$s points"
            AppLanguage.DE -> "Du hast %1\$s Punkte erzielt"
            AppLanguage.IT -> "Hai ottenuto %1\$s punti"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun yearguess_you_label(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You: %1\$s"
            AppLanguage.ES -> "Tú: %1\$s"
            AppLanguage.FR -> "Toi : %1\$s"
            AppLanguage.DE -> "Du: %1\$s"
            AppLanguage.IT -> "Tu: %1\$s"
        }
        return formatTemplate(template, arg1)
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

    @Composable
    fun casualties_actual_casualties(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Actual casualties: %1\$s"
            AppLanguage.ES -> "Bajas reales: %1\$s"
            AppLanguage.FR -> "Pertes réelles : %1\$s"
            AppLanguage.DE -> "Tatsächliche Verluste: %1\$s"
            AppLanguage.IT -> "Vittime reali: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun casualties_earned_multiplier(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You earned %1\$s"
            AppLanguage.ES -> "Ganaste %1\$s"
            AppLanguage.FR -> "Tu as gagné %1\$s"
            AppLanguage.DE -> "Du hast %1\$s erzielt"
            AppLanguage.IT -> "Hai ottenuto %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun casualties_actual_label(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Actual: %1\$s"
            AppLanguage.ES -> "Real: %1\$s"
            AppLanguage.FR -> "Réel : %1\$s"
            AppLanguage.DE -> "Tatsächlich: %1\$s"
            AppLanguage.IT -> "Reale: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun casualties_you_label(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You: %1\$s"
            AppLanguage.ES -> "Tú: %1\$s"
            AppLanguage.FR -> "Toi : %1\$s"
            AppLanguage.DE -> "Du: %1\$s"
            AppLanguage.IT -> "Tu: %1\$s"
        }
        return formatTemplate(template, arg1)
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
