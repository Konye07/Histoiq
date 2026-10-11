package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable

object StringsGamesFlagTerritoryBlindmap {
    val flag_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Match the Flag Color"
            AppLanguage.ES -> "Combina el Color de la Bandera"
            AppLanguage.FR -> "Trouvez la Couleur du Drapeau"
            AppLanguage.DE -> "Flaggenfarbe Treffen"
            AppLanguage.IT -> "Indovina il Colore della Bandiera"
            AppLanguage.HU -> "Találd el a zászló színét"
        }

    val flag_back: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back"
            AppLanguage.ES -> "Atrás"
            AppLanguage.FR -> "Retour"
            AppLanguage.DE -> "Zurück"
            AppLanguage.IT -> "Indietro"
            AppLanguage.HU -> "Vissza"
        }

    @Composable
    fun flag_question_counter(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Question %1\$d / %2\$d"
            AppLanguage.ES -> "Pregunta %1\$d / %2\$d"
            AppLanguage.FR -> "Question %1\$d / %2\$d"
            AppLanguage.DE -> "Frage %1\$d / %2\$d"
            AppLanguage.IT -> "Domanda %1\$d / %2\$d"
            AppLanguage.HU -> "%1\$d. kérdés / %2\$d"
        }
        return formatTemplate(template, arg1, arg2)
    }

    val flag_your_guess: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Your Guess"
            AppLanguage.ES -> "Tu Suposición"
            AppLanguage.FR -> "Votre Essai"
            AppLanguage.DE -> "Deine Vermutung"
            AppLanguage.IT -> "Il Tuo Tentativo"
            AppLanguage.HU -> "A tipped"
        }

    val flag_original: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Original"
            AppLanguage.ES -> "Original"
            AppLanguage.FR -> "Original"
            AppLanguage.DE -> "Original"
            AppLanguage.IT -> "Originale"
            AppLanguage.HU -> "Eredeti"
        }

    val flag_hue: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Hue"
            AppLanguage.ES -> "Tono"
            AppLanguage.FR -> "Teinte"
            AppLanguage.DE -> "Farbton"
            AppLanguage.IT -> "Tonalità"
            AppLanguage.HU -> "Színárnyalat"
        }

    val flag_saturation: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Saturation"
            AppLanguage.ES -> "Saturación"
            AppLanguage.FR -> "Saturation"
            AppLanguage.DE -> "Sättigung"
            AppLanguage.IT -> "Saturazione"
            AppLanguage.HU -> "Telítettség"
        }

    val flag_brightness: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Brightness"
            AppLanguage.ES -> "Brillo"
            AppLanguage.FR -> "Luminosité"
            AppLanguage.DE -> "Helligkeit"
            AppLanguage.IT -> "Luminosità"
            AppLanguage.HU -> "Fényerő"
        }

    val flag_lock_color: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Lock Color ✓"
            AppLanguage.ES -> "Bloquear Color ✓"
            AppLanguage.FR -> "Valider la Couleur ✓"
            AppLanguage.DE -> "Farbe Festlegen ✓"
            AppLanguage.IT -> "Blocca Colore ✓"
            AppLanguage.HU -> "Szín rögzítése ✓"
        }

    @Composable
    fun flag_target_values(arg1: Any, arg2: Any, arg3: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Target: H%1\$d S%2\$d B%3\$d"
            AppLanguage.ES -> "Objetivo: T%1\$d S%2\$d B%3\$d"
            AppLanguage.FR -> "Cible : T%1\$d S%2\$d L%3\$d"
            AppLanguage.DE -> "Ziel: F%1\$d S%2\$d H%3\$d"
            AppLanguage.IT -> "Obiettivo: T%1\$d S%2\$d L%3\$d"
            AppLanguage.HU -> "Cél: H%1\$d S%2\$d B%3\$d"
        }
        return formatTemplate(template, arg1, arg2, arg3)
    }

    @Composable
    fun flag_score_points(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You scored %1\$s points"
            AppLanguage.ES -> "Obtuviste %1\$s puntos"
            AppLanguage.FR -> "Vous avez obtenu %1\$s points"
            AppLanguage.DE -> "Du hast %1\$s Punkte erzielt"
            AppLanguage.IT -> "Hai totalizzato %1\$s punti"
            AppLanguage.HU -> "%1\$s pontot szereztél"
        }
        return formatTemplate(template, arg1)
    }

    val flag_see_results: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "See Results"
            AppLanguage.ES -> "Ver Resultados"
            AppLanguage.FR -> "Voir les Résultats"
            AppLanguage.DE -> "Ergebnisse ansehen"
            AppLanguage.IT -> "Vedi Risultati"
            AppLanguage.HU -> "Eredmények megtekintése"
        }

    val flag_next_question: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Next Question"
            AppLanguage.ES -> "Siguiente Pregunta"
            AppLanguage.FR -> "Question Suivante"
            AppLanguage.DE -> "Nächste Frage"
            AppLanguage.IT -> "Domanda Successiva"
            AppLanguage.HU -> "Következő kérdés"
        }

    @Composable
    fun flag_score_pts(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s pts"
            AppLanguage.ES -> "%1\$s pts"
            AppLanguage.FR -> "%1\$s pts"
            AppLanguage.DE -> "%1\$s Pkt."
            AppLanguage.IT -> "%1\$s pt"
            AppLanguage.HU -> "%1\$s pont"
        }
        return formatTemplate(template, arg1)
    }

    val flag_back_to_menu: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back to Menu"
            AppLanguage.ES -> "Volver al Menú"
            AppLanguage.FR -> "Retour au Menu"
            AppLanguage.DE -> "Zurück zum Menü"
            AppLanguage.IT -> "Torna al Menu"
            AppLanguage.HU -> "Vissza a menübe"
        }

    val territory_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Bigger Territory"
            AppLanguage.ES -> "Territorio Más Grande"
            AppLanguage.FR -> "Plus Grand Territoire"
            AppLanguage.DE -> "Größeres Territorium"
            AppLanguage.IT -> "Territorio Più Grande"
            AppLanguage.HU -> "Nagyobb terület"
        }

    val territory_back: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back"
            AppLanguage.ES -> "Atrás"
            AppLanguage.FR -> "Retour"
            AppLanguage.DE -> "Zurück"
            AppLanguage.IT -> "Indietro"
            AppLanguage.HU -> "Vissza"
        }

    @Composable
    fun territory_round_counter(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Round %1\$d / %2\$d"
            AppLanguage.ES -> "Ronda %1\$d / %2\$d"
            AppLanguage.FR -> "Manche %1\$d / %2\$d"
            AppLanguage.DE -> "Runde %1\$d / %2\$d"
            AppLanguage.IT -> "Turno %1\$d / %2\$d"
            AppLanguage.HU -> "%1\$d. forduló / %2\$d"
        }
        return formatTemplate(template, arg1, arg2)
    }

    val territory_question_prompt: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Which had the bigger territory at its prime?"
            AppLanguage.ES -> "¿Cuál tenía el territorio más grande en su apogeo?"
            AppLanguage.FR -> "Lequel avait le plus grand territoire à son apogée ?"
            AppLanguage.DE -> "Wer hatte auf dem Höhepunkt das größere Territorium?"
            AppLanguage.IT -> "Quale aveva il territorio più vasto al suo apice?"
            AppLanguage.HU -> "Melyiknek volt nagyobb területe a fénykorában?"
        }

    val territory_correct: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Correct!"
            AppLanguage.ES -> "¡Correcto!"
            AppLanguage.FR -> "Correct !"
            AppLanguage.DE -> "Richtig!"
            AppLanguage.IT -> "Corretto!"
            AppLanguage.HU -> "Helyes!"
        }

    val territory_wrong_game_over: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Wrong — game over."
            AppLanguage.ES -> "Incorrecto — fin del juego."
            AppLanguage.FR -> "Faux — partie terminée."
            AppLanguage.DE -> "Falsch — Spiel vorbei."
            AppLanguage.IT -> "Sbagliato — partita finita."
            AppLanguage.HU -> "Rossz — vége a játéknak."
        }

    val territory_see_results: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "See Results"
            AppLanguage.ES -> "Ver Resultados"
            AppLanguage.FR -> "Voir les Résultats"
            AppLanguage.DE -> "Ergebnisse ansehen"
            AppLanguage.IT -> "Vedi Risultati"
            AppLanguage.HU -> "Eredmények megtekintése"
        }

    val territory_next_round: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Next Round"
            AppLanguage.ES -> "Siguiente Ronda"
            AppLanguage.FR -> "Manche Suivante"
            AppLanguage.DE -> "Nächste Runde"
            AppLanguage.IT -> "Turno Successivo"
            AppLanguage.HU -> "Következő forduló"
        }

    @Composable
    fun territory_area_km2(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s km²"
            AppLanguage.ES -> "%1\$s km²"
            AppLanguage.FR -> "%1\$s km²"
            AppLanguage.DE -> "%1\$s km²"
            AppLanguage.IT -> "%1\$s km²"
            AppLanguage.HU -> "%1\$s km²"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun territory_survived_all(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You survived all %1\$d rounds!"
            AppLanguage.ES -> "¡Sobreviviste las %1\$d rondas!"
            AppLanguage.FR -> "Vous avez survécu aux %1\$d manches !"
            AppLanguage.DE -> "Du hast alle %1\$d Runden überstanden!"
            AppLanguage.IT -> "Hai superato tutti i %1\$d turni!"
            AppLanguage.HU -> "Mind a(z) %1\$d fordulót túlélted!"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun territory_made_it_to_round(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You made it to round %1\$d."
            AppLanguage.ES -> "Llegaste a la ronda %1\$d."
            AppLanguage.FR -> "Vous êtes arrivé à la manche %1\$d."
            AppLanguage.DE -> "Du hast es bis Runde %1\$d geschafft."
            AppLanguage.IT -> "Sei arrivato al turno %1\$d."
            AppLanguage.HU -> "A(z) %1\$d. fordulóig jutottál."
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun territory_round_matchup(arg1: Any, arg2: Any, arg3: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Round %1\$d: %2\$s vs %3\$s"
            AppLanguage.ES -> "Ronda %1\$d: %2\$s vs %3\$s"
            AppLanguage.FR -> "Manche %1\$d : %2\$s contre %3\$s"
            AppLanguage.DE -> "Runde %1\$d: %2\$s gegen %3\$s"
            AppLanguage.IT -> "Turno %1\$d: %2\$s contro %3\$s"
            AppLanguage.HU -> "%1\$d. forduló: %2\$s – %3\$s"
        }
        return formatTemplate(template, arg1, arg2, arg3)
    }

    val territory_result_correct: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Correct"
            AppLanguage.ES -> "Correcto"
            AppLanguage.FR -> "Correct"
            AppLanguage.DE -> "Richtig"
            AppLanguage.IT -> "Corretto"
            AppLanguage.HU -> "Helyes"
        }

    val territory_result_wrong: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Wrong"
            AppLanguage.ES -> "Incorrecto"
            AppLanguage.FR -> "Faux"
            AppLanguage.DE -> "Falsch"
            AppLanguage.IT -> "Sbagliato"
            AppLanguage.HU -> "Rossz"
        }

    val territory_failed_here: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "This is where you lost"
            AppLanguage.ES -> "Aquí perdiste"
            AppLanguage.FR -> "C'est ici que vous avez perdu"
            AppLanguage.DE -> "Hier hast du verloren"
            AppLanguage.IT -> "Qui hai perso"
            AppLanguage.HU -> "Itt buktál el"
        }

    val territory_unplayed_header: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Rounds You Didn't Reach"
            AppLanguage.ES -> "Rondas No Alcanzadas"
            AppLanguage.FR -> "Manches Non Atteintes"
            AppLanguage.DE -> "Nicht Erreichte Runden"
            AppLanguage.IT -> "Turni Non Raggiunti"
            AppLanguage.HU -> "Fordulók, amelyekig nem jutottál el"
        }

    @Composable
    fun territory_unplayed_bigger(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Bigger territory: %1\$s"
            AppLanguage.ES -> "Territorio más grande: %1\$s"
            AppLanguage.FR -> "Plus grand territoire : %1\$s"
            AppLanguage.DE -> "Größeres Territorium: %1\$s"
            AppLanguage.IT -> "Territorio più grande: %1\$s"
            AppLanguage.HU -> "Nagyobb terület: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    val territory_back_to_menu: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back to Menu"
            AppLanguage.ES -> "Volver al Menú"
            AppLanguage.FR -> "Retour au Menu"
            AppLanguage.DE -> "Zurück zum Menü"
            AppLanguage.IT -> "Torna al Menu"
            AppLanguage.HU -> "Vissza a menübe"
        }

    val blindmap_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Blind Map Battle"
            AppLanguage.ES -> "Batalla de Mapa a Ciegas"
            AppLanguage.FR -> "Bataille de Carte à l'Aveugle"
            AppLanguage.DE -> "Blinde Kartenschlacht"
            AppLanguage.IT -> "Battaglia sulla Mappa al Buio"
            AppLanguage.HU -> "Vak térképcsata"
        }

    val blindmap_back: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back"
            AppLanguage.ES -> "Atrás"
            AppLanguage.FR -> "Retour"
            AppLanguage.DE -> "Zurück"
            AppLanguage.IT -> "Indietro"
            AppLanguage.HU -> "Vissza"
        }

    @Composable
    fun blindmap_question_counter(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Question %1\$d / %2\$d"
            AppLanguage.ES -> "Pregunta %1\$d / %2\$d"
            AppLanguage.FR -> "Question %1\$d / %2\$d"
            AppLanguage.DE -> "Frage %1\$d / %2\$d"
            AppLanguage.IT -> "Domanda %1\$d / %2\$d"
            AppLanguage.HU -> "%1\$d. kérdés / %2\$d"
        }
        return formatTemplate(template, arg1, arg2)
    }

    val blindmap_instructions: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Pinch to zoom, drag to pan, tap to place your pin — no borders shown."
            AppLanguage.ES -> "Pellizca para hacer zoom, arrastra para desplazarte, toca para colocar tu pin — sin fronteras visibles."
            AppLanguage.FR -> "Pincez pour zoomer, faites glisser pour vous déplacer, touchez pour placer votre repère — aucune frontière affichée."
            AppLanguage.DE -> "Zum Zoomen zwei Finger nutzen, zum Verschieben ziehen, zum Platzieren deiner Nadel tippen — keine Grenzen sichtbar."
            AppLanguage.IT -> "Pizzica per ingrandire, trascina per spostarti, tocca per posizionare il tuo segnalino — nessun confine mostrato."
            AppLanguage.HU -> "Csippentéssel nagyíthatsz, húzással mozgathatod a térképet, koppintással helyezed el a jelölőt — határok nélkül."
        }

    val blindmap_map_attribution: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Map: Natural Earth data via Wikimedia Commons (CC BY-SA 4.0)"
            AppLanguage.ES -> "Mapa: datos de Natural Earth vía Wikimedia Commons (CC BY-SA 4.0)"
            AppLanguage.FR -> "Carte : données Natural Earth via Wikimedia Commons (CC BY-SA 4.0)"
            AppLanguage.DE -> "Karte: Natural Earth-Daten über Wikimedia Commons (CC BY-SA 4.0)"
            AppLanguage.IT -> "Mappa: dati Natural Earth tramite Wikimedia Commons (CC BY-SA 4.0)"
            AppLanguage.HU -> "Térkép: Natural Earth adatok a Wikimedia Commonson keresztül (CC BY-SA 4.0)"
        }

    val blindmap_submit: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Submit"
            AppLanguage.ES -> "Enviar"
            AppLanguage.FR -> "Valider"
            AppLanguage.DE -> "Bestätigen"
            AppLanguage.IT -> "Invia"
            AppLanguage.HU -> "Beküldés"
        }

    @Composable
    fun blindmap_score_points(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You scored %1\$s points"
            AppLanguage.ES -> "Obtuviste %1\$s puntos"
            AppLanguage.FR -> "Vous avez obtenu %1\$s points"
            AppLanguage.DE -> "Du hast %1\$s Punkte erzielt"
            AppLanguage.IT -> "Hai totalizzato %1\$s punti"
            AppLanguage.HU -> "%1\$s pontot szereztél"
        }
        return formatTemplate(template, arg1)
    }

    val blindmap_see_results: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "See Results"
            AppLanguage.ES -> "Ver Resultados"
            AppLanguage.FR -> "Voir les Résultats"
            AppLanguage.DE -> "Ergebnisse ansehen"
            AppLanguage.IT -> "Vedi Risultati"
            AppLanguage.HU -> "Eredmények megtekintése"
        }

    val blindmap_next_question: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Next Question"
            AppLanguage.ES -> "Siguiente Pregunta"
            AppLanguage.FR -> "Question Suivante"
            AppLanguage.DE -> "Nächste Frage"
            AppLanguage.IT -> "Domanda Successiva"
            AppLanguage.HU -> "Következő kérdés"
        }

    @Composable
    fun blindmap_pin_actual_pts(arg1: Any, arg2: Any, arg3: Any, arg4: Any, arg5: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Your pin: %1\$s, %2\$s  ·  Actual: %3\$s, %4\$s  ·  %5\$s pts"
            AppLanguage.ES -> "Tu pin: %1\$s, %2\$s  ·  Real: %3\$s, %4\$s  ·  %5\$s pts"
            AppLanguage.FR -> "Votre repère : %1\$s, %2\$s  ·  Réel : %3\$s, %4\$s  ·  %5\$s pts"
            AppLanguage.DE -> "Deine Nadel: %1\$s, %2\$s  ·  Tatsächlich: %3\$s, %4\$s  ·  %5\$s Pkt."
            AppLanguage.IT -> "Il tuo segnalino: %1\$s, %2\$s  ·  Effettivo: %3\$s, %4\$s  ·  %5\$s pt"
            AppLanguage.HU -> "A jelölőd: %1\$s, %2\$s  ·  Valós hely: %3\$s, %4\$s  ·  %5\$s pont"
        }
        return formatTemplate(template, arg1, arg2, arg3, arg4, arg5)
    }

    @Composable
    fun blindmap_score_pts(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s pts"
            AppLanguage.ES -> "%1\$s pts"
            AppLanguage.FR -> "%1\$s pts"
            AppLanguage.DE -> "%1\$s Pkt."
            AppLanguage.IT -> "%1\$s pt"
            AppLanguage.HU -> "%1\$s pont"
        }
        return formatTemplate(template, arg1)
    }

    val blindmap_back_to_menu: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back to Menu"
            AppLanguage.ES -> "Volver al Menú"
            AppLanguage.FR -> "Retour au Menu"
            AppLanguage.DE -> "Zurück zum Menü"
            AppLanguage.IT -> "Torna al Menu"
            AppLanguage.HU -> "Vissza a menübe"
        }

}
