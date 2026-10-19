package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable

object StringsDailyBattle {
    /** arg1: the most a battle can score, already formatted ("6 000"). */
    @Composable
    fun battle_intro(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "One question from each of the six games, drawn from today's best-known events, battles, flags and facts. Your six answers add up to one score out of %1\$s. Take them in order and stop whenever you like: your progress is saved."
            AppLanguage.ES -> "Una pregunta de cada uno de los seis juegos, elegidas entre los hechos, batallas, banderas y datos más conocidos de hoy. Tus seis respuestas suman una única puntuación sobre %1\$s. Respóndelas en orden y para cuando quieras: tu progreso se guarda."
            AppLanguage.FR -> "Une question de chacun des six jeux, choisie parmi les événements, batailles, drapeaux et faits les plus connus du jour. Tes six réponses s'additionnent en un seul score sur %1\$s. Réponds-y dans l'ordre et arrête-toi quand tu veux : ta progression est sauvegardée."
            AppLanguage.DE -> "Eine Frage aus jedem der sechs Spiele, ausgewählt aus den bekanntesten Ereignissen, Schlachten, Flaggen und Fakten des Tages. Deine sechs Antworten ergeben zusammen ein Ergebnis von höchstens %1\$s. Beantworte sie der Reihe nach und hör auf, wann du willst: Dein Fortschritt wird gespeichert."
            AppLanguage.IT -> "Una domanda per ciascuno dei sei giochi, scelta tra gli eventi, le battaglie, le bandiere e i fatti più noti di oggi. Le tue sei risposte si sommano in un unico punteggio su %1\$s. Rispondi in ordine e fermati quando vuoi: i tuoi progressi vengono salvati."
            AppLanguage.HU -> "Egy kérdés a hat játék mindegyikéből, a mai nap legismertebb eseményei, csatái, zászlói és tényei közül válogatva. A hat válaszod egyetlen, legfeljebb %1\$s pontos összpontszámmá adódik össze. Válaszolj rájuk sorban, és hagyd abba, amikor akarod: az állásod mentődik."
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun battle_points_so_far(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s points so far"
            AppLanguage.ES -> "%1\$s puntos hasta ahora"
            AppLanguage.FR -> "%1\$s points pour l'instant"
            AppLanguage.DE -> "%1\$s Punkte bisher"
            AppLanguage.IT -> "%1\$s punti finora"
            AppLanguage.HU -> "Eddig %1\$s pont"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun battle_questions_answered(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s of %2\$s questions answered"
            AppLanguage.ES -> "%1\$s de %2\$s preguntas respondidas"
            AppLanguage.FR -> "%1\$s sur %2\$s questions répondues"
            AppLanguage.DE -> "%1\$s von %2\$s Fragen beantwortet"
            AppLanguage.IT -> "%1\$s domande su %2\$s risposte"
            AppLanguage.HU -> "%1\$s/%2\$s kérdés megválaszolva"
        }
        return formatTemplate(template, arg1, arg2)
    }

    @Composable
    fun battle_start_with(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Start with %1\$s"
            AppLanguage.ES -> "Empezar con %1\$s"
            AppLanguage.FR -> "Commencer par %1\$s"
            AppLanguage.DE -> "Mit %1\$s beginnen"
            AppLanguage.IT -> "Inizia con %1\$s"
            AppLanguage.HU -> "Kezdés: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun battle_continue_with(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Continue with %1\$s"
            AppLanguage.ES -> "Continuar con %1\$s"
            AppLanguage.FR -> "Continuer avec %1\$s"
            AppLanguage.DE -> "Weiter mit %1\$s"
            AppLanguage.IT -> "Continua con %1\$s"
            AppLanguage.HU -> "Tovább: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    val battle_up_next: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Up next"
            AppLanguage.ES -> "Siguiente"
            AppLanguage.FR -> "À suivre"
            AppLanguage.DE -> "Als Nächstes"
            AppLanguage.IT -> "Prossimo"
            AppLanguage.HU -> "Következik"
        }

    /** The line under a game's title in the app bar while its question is asked inside the Daily Battle. */
    @Composable
    fun battle_bar_title(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Daily Battle · question %1\$s of %2\$s"
            AppLanguage.ES -> "Batalla Diaria · pregunta %1\$s de %2\$s"
            AppLanguage.FR -> "Bataille du Jour · question %1\$s sur %2\$s"
            AppLanguage.DE -> "Tagesschlacht · Frage %1\$s von %2\$s"
            AppLanguage.IT -> "Battaglia del Giorno · domanda %1\$s di %2\$s"
            AppLanguage.HU -> "Napi Csata · %1\$s. kérdés a %2\$s közül"
        }
        return formatTemplate(template, arg1, arg2)
    }

    val battle_results_your_answers: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Your six answers"
            AppLanguage.ES -> "Tus seis respuestas"
            AppLanguage.FR -> "Tes six réponses"
            AppLanguage.DE -> "Deine sechs Antworten"
            AppLanguage.IT -> "Le tue sei risposte"
            AppLanguage.HU -> "A hat válaszod"
        }

    /** After a wrong answer to the Cosmographer or Chronicler question: no "game over", it is one question of six. */
    val battle_wrong: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Wrong."
            AppLanguage.ES -> "Incorrecto."
            AppLanguage.FR -> "Faux."
            AppLanguage.DE -> "Falsch."
            AppLanguage.IT -> "Sbagliato."
            AppLanguage.HU -> "Rossz."
        }

    val battle_back_to_menu: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back to menu"
            AppLanguage.ES -> "Volver al menú"
            AppLanguage.FR -> "Retour au menu"
            AppLanguage.DE -> "Zurück zum Menü"
            AppLanguage.IT -> "Torna al menu"
            AppLanguage.HU -> "Vissza a menübe"
        }

    val battle_back_cd: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back"
            AppLanguage.ES -> "Atrás"
            AppLanguage.FR -> "Retour"
            AppLanguage.DE -> "Zurück"
            AppLanguage.IT -> "Indietro"
            AppLanguage.HU -> "Vissza"
        }
}
