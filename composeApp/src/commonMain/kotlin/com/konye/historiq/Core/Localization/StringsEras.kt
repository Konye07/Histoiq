package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable
import com.konye.historiq.Data.Quiz.QuizEra

object StringsEras {
    /** [era]'s name, or the All-time label when null. */
    @Composable
    fun label(era: QuizEra?): String = when (era) {
        null -> era_all_time
        QuizEra.ANCIENT -> era_ancient
        QuizEra.MEDIEVAL -> era_medieval
        QuizEra.EARLY_MODERN -> era_early_modern
        QuizEra.MODERN -> era_modern
        QuizEra.CONTEMPORARY -> era_contemporary
    }

    /** The years [era] covers, or a plain description of All-time when null. */
    @Composable
    fun range(era: QuizEra?): String = when (era) {
        null -> era_range_all_time
        QuizEra.ANCIENT -> era_range_ancient
        QuizEra.MEDIEVAL -> "476–1453"
        QuizEra.EARLY_MODERN -> "1453–1750"
        QuizEra.MODERN -> "1750–1899"
        QuizEra.CONTEMPORARY -> era_range_contemporary
    }

    val era_all_time: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "All-time"
            AppLanguage.ES -> "Todas las épocas"
            AppLanguage.FR -> "Toutes les époques"
            AppLanguage.DE -> "Alle Epochen"
            AppLanguage.IT -> "Tutte le epoche"
            AppLanguage.HU -> "Minden korszak"
        }

    val era_ancient: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Ancient"
            AppLanguage.ES -> "Antigua"
            AppLanguage.FR -> "Antiquité"
            AppLanguage.DE -> "Antike"
            AppLanguage.IT -> "Antichità"
            AppLanguage.HU -> "Ókor"
        }

    val era_medieval: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Medieval"
            AppLanguage.ES -> "Medieval"
            AppLanguage.FR -> "Moyen Âge"
            AppLanguage.DE -> "Mittelalter"
            AppLanguage.IT -> "Medioevo"
            AppLanguage.HU -> "Középkor"
        }

    val era_early_modern: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Early Modern"
            AppLanguage.ES -> "Moderna temprana"
            AppLanguage.FR -> "Début de l'ère moderne"
            AppLanguage.DE -> "Frühe Neuzeit"
            AppLanguage.IT -> "Prima età moderna"
            AppLanguage.HU -> "Kora újkor"
        }

    val era_modern: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Modern"
            AppLanguage.ES -> "Moderna"
            AppLanguage.FR -> "Ère moderne"
            AppLanguage.DE -> "Neuzeit"
            AppLanguage.IT -> "Età moderna"
            AppLanguage.HU -> "Újkor"
        }

    val era_contemporary: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Contemporary"
            AppLanguage.ES -> "Contemporánea"
            AppLanguage.FR -> "Ère contemporaine"
            AppLanguage.DE -> "Zeitgeschichte"
            AppLanguage.IT -> "Età contemporanea"
            AppLanguage.HU -> "Jelenkor"
        }

    val era_range_all_time: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Questions from every era"
            AppLanguage.ES -> "Preguntas de todas las épocas"
            AppLanguage.FR -> "Des questions de toutes les époques"
            AppLanguage.DE -> "Fragen aus allen Epochen"
            AppLanguage.IT -> "Domande di tutte le epoche"
            AppLanguage.HU -> "Kérdések minden korszakból"
        }

    val era_range_ancient: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Up to 476 CE"
            AppLanguage.ES -> "Hasta el año 476"
            AppLanguage.FR -> "Jusqu'en 476"
            AppLanguage.DE -> "Bis 476 n. Chr."
            AppLanguage.IT -> "Fino al 476 d.C."
            AppLanguage.HU -> "Kr. u. 476-ig"
        }

    val era_range_contemporary: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "20th–21st century"
            AppLanguage.ES -> "Siglos XX y XXI"
            AppLanguage.FR -> "XXe–XXIe siècle"
            AppLanguage.DE -> "20.–21. Jahrhundert"
            AppLanguage.IT -> "XX–XXI secolo"
            AppLanguage.HU -> "20–21. század"
        }

    @Composable
    fun era_chip_label(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Era: %1\$s"
            AppLanguage.ES -> "Época: %1\$s"
            AppLanguage.FR -> "Époque : %1\$s"
            AppLanguage.DE -> "Epoche: %1\$s"
            AppLanguage.IT -> "Epoca: %1\$s"
            AppLanguage.HU -> "Korszak: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    val era_picker_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Filter by era"
            AppLanguage.ES -> "Filtrar por época"
            AppLanguage.FR -> "Filtrer par époque"
            AppLanguage.DE -> "Nach Epoche filtern"
            AppLanguage.IT -> "Filtra per epoca"
            AppLanguage.HU -> "Szűrés korszak szerint"
        }

    @Composable
    fun era_picker_note(arg1: Any, arg2: Any, arg3: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Shows the stats of rounds played in an era's own game mode (%1\$s, %2\$s and %3\$s). The other games only have all-time stats."
            AppLanguage.ES -> "Muestra las estadísticas de las rondas jugadas en el modo de juego propio de una época (%1\$s, %2\$s y %3\$s). Los demás juegos solo tienen estadísticas de todas las épocas."
            AppLanguage.FR -> "Affiche les statistiques des manches jouées dans le mode de jeu propre à une époque (%1\$s, %2\$s et %3\$s). Les autres jeux n'ont que des statistiques toutes époques."
            AppLanguage.DE -> "Zeigt die Statistiken der Runden aus dem eigenen Spielmodus einer Epoche (%1\$s, %2\$s und %3\$s). Die anderen Spiele haben nur Statistiken über alle Epochen."
            AppLanguage.IT -> "Mostra le statistiche dei round giocati nella modalità di gioco propria di un'epoca (%1\$s, %2\$s e %3\$s). Gli altri giochi hanno solo statistiche di tutte le epoche."
            AppLanguage.HU -> "Az egyes korszakok saját játékmódjában (%1\$s, %2\$s és %3\$s) lejátszott körök statisztikáit mutatja. A többi játéknak csak az összes korszakra vonatkozó statisztikája van."
        }
        return formatTemplate(template, arg1, arg2, arg3)
    }

    @Composable
    fun era_mode_blurb(arg1: Any, arg2: Any, arg3: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "A game mode of its own: a fresh daily round of %1\$s, %2\$s and %3\$s with questions from this era only, and its own streak, rank and stats."
            AppLanguage.ES -> "Un modo de juego propio: una ronda diaria nueva de %1\$s, %2\$s y %3\$s con preguntas solo de esta época, y con su propia racha, clasificación y estadísticas."
            AppLanguage.FR -> "Un mode de jeu à part entière : une nouvelle manche quotidienne de %1\$s, %2\$s et %3\$s avec des questions de cette époque uniquement, et sa propre série, son propre classement et ses propres statistiques."
            AppLanguage.DE -> "Ein eigener Spielmodus: eine frische Tagesrunde für %1\$s, %2\$s und %3\$s mit Fragen nur aus dieser Epoche, samt eigener Serie, eigenem Rang und eigenen Statistiken."
            AppLanguage.IT -> "Una modalità di gioco a sé: un nuovo round giornaliero di %1\$s, %2\$s e %3\$s con domande solo di questa epoca, e con la propria serie, classifica e statistiche."
            AppLanguage.HU -> "Önálló játékmód: friss napi kör a következőkből: %1\$s, %2\$s és %3\$s, csak ebből a korszakból származó kérdésekkel, saját sorozattal, ranggal és statisztikával."
        }
        return formatTemplate(template, arg1, arg2, arg3)
    }
}
