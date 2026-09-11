package com.yourorg.app.Core.Localization

import androidx.compose.runtime.Composable

object StringsGamesTruefalse {
    val tf_back: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back"
            AppLanguage.ES -> "Atrás"
            AppLanguage.FR -> "Retour"
            AppLanguage.DE -> "Zurück"
            AppLanguage.IT -> "Indietro"
        }

    @Composable
    fun tf_round_counter(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Round %1\$d / %2\$d"
            AppLanguage.ES -> "Ronda %1\$d / %2\$d"
            AppLanguage.FR -> "Manche %1\$d / %2\$d"
            AppLanguage.DE -> "Runde %1\$d / %2\$d"
            AppLanguage.IT -> "Turno %1\$d / %2\$d"
        }
        return formatTemplate(template, arg1, arg2)
    }

    val tf_true_button: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "True"
            AppLanguage.ES -> "Verdadero"
            AppLanguage.FR -> "Vrai"
            AppLanguage.DE -> "Wahr"
            AppLanguage.IT -> "Vero"
        }

    val tf_false_button: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "False"
            AppLanguage.ES -> "Falso"
            AppLanguage.FR -> "Faux"
            AppLanguage.DE -> "Falsch"
            AppLanguage.IT -> "Falso"
        }

    val tf_true_caps: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "TRUE"
            AppLanguage.ES -> "VERDADERO"
            AppLanguage.FR -> "VRAI"
            AppLanguage.DE -> "WAHR"
            AppLanguage.IT -> "VERO"
        }

    val tf_false_caps: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "FALSE"
            AppLanguage.ES -> "FALSO"
            AppLanguage.FR -> "FAUX"
            AppLanguage.DE -> "FALSCH"
            AppLanguage.IT -> "FALSO"
        }

    val tf_correct: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Correct!"
            AppLanguage.ES -> "¡Correcto!"
            AppLanguage.FR -> "Correct !"
            AppLanguage.DE -> "Richtig!"
            AppLanguage.IT -> "Corretto!"
        }

    val tf_wrong_game_over: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Wrong — game over."
            AppLanguage.ES -> "Incorrecto — fin del juego."
            AppLanguage.FR -> "Faux — partie terminée."
            AppLanguage.DE -> "Falsch — Spiel vorbei."
            AppLanguage.IT -> "Sbagliato — partita finita."
        }

    @Composable
    fun tf_statement_is(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "The statement is %1\$s."
            AppLanguage.ES -> "La afirmación es %1\$s."
            AppLanguage.FR -> "L'affirmation est %1\$s."
            AppLanguage.DE -> "Die Aussage ist %1\$s."
            AppLanguage.IT -> "L'affermazione è %1\$s."
        }
        return formatTemplate(template, arg1)
    }

    val tf_see_results: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "See Results"
            AppLanguage.ES -> "Ver Resultados"
            AppLanguage.FR -> "Voir les Résultats"
            AppLanguage.DE -> "Ergebnisse Ansehen"
            AppLanguage.IT -> "Vedi Risultati"
        }

    val tf_next_statement: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Next Statement"
            AppLanguage.ES -> "Siguiente Afirmación"
            AppLanguage.FR -> "Affirmation Suivante"
            AppLanguage.DE -> "Nächste Aussage"
            AppLanguage.IT -> "Affermazione Successiva"
        }

    @Composable
    fun tf_survived_all(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You survived all %1\$d statements!"
            AppLanguage.ES -> "¡Sobreviviste las %1\$d afirmaciones!"
            AppLanguage.FR -> "Tu as survécu aux %1\$d affirmations !"
            AppLanguage.DE -> "Du hast alle %1\$d Aussagen überstanden!"
            AppLanguage.IT -> "Hai superato tutte le %1\$d affermazioni!"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun tf_made_it_to(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You made it to statement %1\$d."
            AppLanguage.ES -> "Llegaste a la afirmación %1\$d."
            AppLanguage.FR -> "Tu es arrivé jusqu'à l'affirmation %1\$d."
            AppLanguage.DE -> "Du hast es bis Aussage %1\$d geschafft."
            AppLanguage.IT -> "Sei arrivato all'affermazione %1\$d."
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun tf_answer_correct_line(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Your answer: %1\$s  ·  Correct: %2\$s"
            AppLanguage.ES -> "Tu respuesta: %1\$s  ·  Correcto: %2\$s"
            AppLanguage.FR -> "Ta réponse : %1\$s  ·  Correct : %2\$s"
            AppLanguage.DE -> "Deine Antwort: %1\$s  ·  Richtig: %2\$s"
            AppLanguage.IT -> "La tua risposta: %1\$s  ·  Corretto: %2\$s"
        }
        return formatTemplate(template, arg1, arg2)
    }

    val tf_failed_here: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "This is where you lost"
            AppLanguage.ES -> "Aquí perdiste"
            AppLanguage.FR -> "C'est ici que tu as perdu"
            AppLanguage.DE -> "Hier hast du verloren"
            AppLanguage.IT -> "Qui hai perso"
        }

    val tf_unplayed_header: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Statements You Didn't Reach"
            AppLanguage.ES -> "Afirmaciones No Alcanzadas"
            AppLanguage.FR -> "Affirmations Non Atteintes"
            AppLanguage.DE -> "Nicht Erreichte Aussagen"
            AppLanguage.IT -> "Affermazioni Non Raggiunte"
        }

    @Composable
    fun tf_unplayed_answer_line(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Correct answer: %1\$s"
            AppLanguage.ES -> "Respuesta correcta: %1\$s"
            AppLanguage.FR -> "Réponse correcte : %1\$s"
            AppLanguage.DE -> "Richtige Antwort: %1\$s"
            AppLanguage.IT -> "Risposta corretta: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    val tf_back_to_menu: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back to Menu"
            AppLanguage.ES -> "Volver al Menú"
            AppLanguage.FR -> "Retour au Menu"
            AppLanguage.DE -> "Zurück zum Menü"
            AppLanguage.IT -> "Torna al Menu"
        }

}
