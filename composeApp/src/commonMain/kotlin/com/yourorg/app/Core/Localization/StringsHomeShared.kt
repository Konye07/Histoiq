package com.yourorg.app.Core.Localization

import androidx.compose.runtime.Composable

object StringsHomeShared {
    val home_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Historiq"
            AppLanguage.ES -> "Historiq"
            AppLanguage.FR -> "Historiq"
            AppLanguage.DE -> "Historiq"
            AppLanguage.IT -> "Historiq"
        }

    val home_desc_year_guess: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "An event is given — guess the year it happened. The closer you are, the more points you score."
            AppLanguage.ES -> "Se muestra un evento: adivina el año en que ocurrió. Cuanto más te acerques, más puntos consigues."
            AppLanguage.FR -> "Un événement est donné — devine l'année à laquelle il a eu lieu. Plus tu es proche, plus tu marques de points."
            AppLanguage.DE -> "Ein Ereignis wird gezeigt — errate, in welchem Jahr es stattfand. Je näher du liegst, desto mehr Punkte bekommst du."
            AppLanguage.IT -> "Viene mostrato un evento: indovina l'anno in cui è accaduto. Più ti avvicini, più punti ottieni."
        }

    val home_desc_casualties_multiplier: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "A battle is given — guess the casualties. The closer your guess, the bigger your multiplier."
            AppLanguage.ES -> "Se muestra una batalla: adivina el número de bajas. Cuanto más te acerques, mayor será tu multiplicador."
            AppLanguage.FR -> "Une bataille est donnée — devine le nombre de pertes. Plus ton estimation est proche, plus ton multiplicateur est élevé."
            AppLanguage.DE -> "Eine Schlacht wird gezeigt — schätze die Verluste. Je näher deine Schätzung ist, desto größer dein Multiplikator."
            AppLanguage.IT -> "Viene mostrata una battaglia: indovina il numero delle vittime. Più la tua stima è vicina, maggiore sarà il tuo moltiplicatore."
        }

    val home_desc_flag_color_match: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Match a historical flag's true color using hue, saturation and brightness sliders."
            AppLanguage.ES -> "Iguala el color real de una bandera histórica ajustando los deslizadores de tono, saturación y brillo."
            AppLanguage.FR -> "Reproduis la couleur réelle d'un drapeau historique à l'aide des curseurs de teinte, saturation et luminosité."
            AppLanguage.DE -> "Triff die wahre Farbe einer historischen Flagge mit den Reglern für Farbton, Sättigung und Helligkeit."
            AppLanguage.IT -> "Abbina il vero colore di una bandiera storica usando i cursori di tonalità, saturazione e luminosità."
        }

    val home_desc_territory_compare: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Pick which empire had the bigger territory at its prime. One wrong pick ends the run."
            AppLanguage.ES -> "Elige qué imperio tuvo el mayor territorio en su apogeo. Un solo fallo termina la partida."
            AppLanguage.FR -> "Choisis quel empire avait le plus grand territoire à son apogée. Une seule erreur met fin à la partie."
            AppLanguage.DE -> "Wähle, welches Reich auf seinem Höhepunkt das größere Territorium hatte. Eine falsche Wahl beendet den Lauf."
            AppLanguage.IT -> "Scegli quale impero avesse il territorio più vasto al suo apice. Una sola scelta sbagliata termina la partita."
        }

    val home_desc_true_false: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Historical statements — one wrong answer ends the run."
            AppLanguage.ES -> "Afirmaciones históricas: una sola respuesta incorrecta termina la partida."
            AppLanguage.FR -> "Des affirmations historiques — une seule mauvaise réponse met fin à la partie."
            AppLanguage.DE -> "Historische Aussagen — eine falsche Antwort beendet den Lauf."
            AppLanguage.IT -> "Affermazioni storiche: una sola risposta sbagliata termina la partita."
        }

    val home_desc_blind_map_battle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Place a historical battle on the map with no borders shown. The closer your pin, the more points."
            AppLanguage.ES -> "Marca en el mapa la ubicación de una batalla histórica sin fronteras visibles. Cuanto más cerca coloques tu marcador, más puntos consigues."
            AppLanguage.FR -> "Place une bataille historique sur la carte, sans aucune frontière affichée. Plus ton repère est précis, plus tu marques de points."
            AppLanguage.DE -> "Markiere den Ort einer historischen Schlacht auf der Karte ohne eingezeichnete Grenzen. Je näher deine Markierung, desto mehr Punkte."
            AppLanguage.IT -> "Posiziona sulla mappa una battaglia storica senza confini visibili. Più il tuo segnaposto è preciso, più punti ottieni."
        }

    val home_coming_soon: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Coming soon"
            AppLanguage.ES -> "Próximamente"
            AppLanguage.FR -> "Bientôt disponible"
            AppLanguage.DE -> "Demnächst verfügbar"
            AppLanguage.IT -> "Prossimamente"
        }

    @Composable
    fun home_about_content_desc(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "About %1\$s"
            AppLanguage.ES -> "Acerca de %1\$s"
            AppLanguage.FR -> "À propos de %1\$s"
            AppLanguage.DE -> "Über %1\$s"
            AppLanguage.IT -> "Informazioni su %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun home_info_dialog_title(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s — %2\$s"
            AppLanguage.ES -> "%1\$s — %2\$s"
            AppLanguage.FR -> "%1\$s — %2\$s"
            AppLanguage.DE -> "%1\$s — %2\$s"
            AppLanguage.IT -> "%1\$s — %2\$s"
        }
        return formatTemplate(template, arg1, arg2)
    }

    val home_close: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Close"
            AppLanguage.ES -> "Cerrar"
            AppLanguage.FR -> "Fermer"
            AppLanguage.DE -> "Schließen"
            AppLanguage.IT -> "Chiudi"
        }

    @Composable
    fun home_countdown(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Test your knowledge again in %1\$s"
            AppLanguage.ES -> "Pon a prueba tus conocimientos de nuevo en %1\$s"
            AppLanguage.FR -> "Teste à nouveau tes connaissances dans %1\$s"
            AppLanguage.DE -> "Teste dein Wissen erneut in %1\$s"
            AppLanguage.IT -> "Metti di nuovo alla prova le tue conoscenze tra %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    val results_final_score: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Final Score"
            AppLanguage.ES -> "Puntuación Final"
            AppLanguage.FR -> "Score Final"
            AppLanguage.DE -> "Endergebnis"
            AppLanguage.IT -> "Punteggio Finale"
        }

    @Composable
    fun results_rank_of_today(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Rank %1\$s of %2\$s today"
            AppLanguage.ES -> "Puesto %1\$s de %2\$s hoy"
            AppLanguage.FR -> "%1\$se sur %2\$s aujourd'hui"
            AppLanguage.DE -> "Platz %1\$s von %2\$s heute"
            AppLanguage.IT -> "%1\$s° su %2\$s oggi"
        }
        return formatTemplate(template, arg1, arg2)
    }

    val results_share: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Share Results"
            AppLanguage.ES -> "Compartir Resultados"
            AppLanguage.FR -> "Partager les Résultats"
            AppLanguage.DE -> "Ergebnisse Teilen"
            AppLanguage.IT -> "Condividi Risultati"
        }

    val results_share_chooser_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Share your results"
            AppLanguage.ES -> "Comparte tus resultados"
            AppLanguage.FR -> "Partage tes résultats"
            AppLanguage.DE -> "Teile deine Ergebnisse"
            AppLanguage.IT -> "Condividi i tuoi risultati"
        }

    @Composable
    fun results_share_message(arg1: Any, arg2: Any, arg3: Any, arg4: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "I scored %1\$s in %2\$s on %3\$s! Can you beat me?\\n\\n%4\$s"
            AppLanguage.ES -> "¡Saqué %1\$s en %2\$s en %3\$s! ¿Puedes superarme?\\n\\n%4\$s"
            AppLanguage.FR -> "J'ai obtenu %1\$s en %2\$s sur %3\$s ! Peux-tu me battre ?\\n\\n%4\$s"
            AppLanguage.DE -> "Ich habe %1\$s in %2\$s bei %3\$s erreicht! Schaffst du mehr?\\n\\n%4\$s"
            AppLanguage.IT -> "Ho ottenuto %1\$s in %2\$s su %3\$s! Riesci a battermi?\\n\\n%4\$s"
        }
        return formatTemplate(template, arg1, arg2, arg3, arg4)
    }

    @Composable
    fun results_share_footer(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Can you beat me? Play %1\$s"
            AppLanguage.ES -> "¿Puedes superarme? Juega %1\$s"
            AppLanguage.FR -> "Peux-tu me battre ? Joue à %1\$s"
            AppLanguage.DE -> "Schaffst du mehr? Spiel %1\$s"
            AppLanguage.IT -> "Riesci a battermi? Gioca a %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    val hint_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Hint"
            AppLanguage.ES -> "Pista"
            AppLanguage.FR -> "Indice"
            AppLanguage.DE -> "Hinweis"
            AppLanguage.IT -> "Suggerimento"
        }

    val hint_dialog_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Want a hint?"
            AppLanguage.ES -> "¿Quieres una pista?"
            AppLanguage.FR -> "Tu veux un indice ?"
            AppLanguage.DE -> "Möchtest du einen Hinweis?"
            AppLanguage.IT -> "Vuoi un suggerimento?"
        }

    val hint_dialog_text: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Watch a short ad to find out which war or conflict this is part of."
            AppLanguage.ES -> "Mira un breve anuncio para descubrir a qué guerra o conflicto pertenece esto."
            AppLanguage.FR -> "Regarde une courte publicité pour découvrir à quelle guerre ou quel conflit cela appartient."
            AppLanguage.DE -> "Sieh dir eine kurze Werbung an, um herauszufinden, zu welchem Krieg oder Konflikt dies gehört."
            AppLanguage.IT -> "Guarda un breve annuncio per scoprire a quale guerra o conflitto appartiene."
        }

    val hint_watch_ad: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Watch Ad"
            AppLanguage.ES -> "Ver Anuncio"
            AppLanguage.FR -> "Voir la Pub"
            AppLanguage.DE -> "Werbung Ansehen"
            AppLanguage.IT -> "Guarda l'Annuncio"
        }

    val hint_cancel: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Cancel"
            AppLanguage.ES -> "Cancelar"
            AppLanguage.FR -> "Annuler"
            AppLanguage.DE -> "Abbrechen"
            AppLanguage.IT -> "Annulla"
        }

    val nav_play: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Play"
            AppLanguage.ES -> "Jugar"
            AppLanguage.FR -> "Jouer"
            AppLanguage.DE -> "Spielen"
            AppLanguage.IT -> "Gioca"
        }

    val nav_friends: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Friends"
            AppLanguage.ES -> "Amigos"
            AppLanguage.FR -> "Amis"
            AppLanguage.DE -> "Freunde"
            AppLanguage.IT -> "Amici"
        }

    val nav_profile: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Profile"
            AppLanguage.ES -> "Perfil"
            AppLanguage.FR -> "Profil"
            AppLanguage.DE -> "Profil"
            AppLanguage.IT -> "Profilo"
        }

    val nav_settings: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Settings"
            AppLanguage.ES -> "Ajustes"
            AppLanguage.FR -> "Paramètres"
            AppLanguage.DE -> "Einstellungen"
            AppLanguage.IT -> "Impostazioni"
        }

    val nav_year_guess: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Guess the Year"
            AppLanguage.ES -> "Adivina el Año"
            AppLanguage.FR -> "Devine l'Année"
            AppLanguage.DE -> "Errate das Jahr"
            AppLanguage.IT -> "Indovina l'Anno"
        }

    val nav_casualties_multiplier: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Casualty Count"
            AppLanguage.ES -> "Cuenta de Bajas"
            AppLanguage.FR -> "Compte des Pertes"
            AppLanguage.DE -> "Verlustzahlen"
            AppLanguage.IT -> "Conta delle Vittime"
        }

    val nav_flag_color_match: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Match the Flag Color"
            AppLanguage.ES -> "Iguala el Color de la Bandera"
            AppLanguage.FR -> "Reproduis la Couleur du Drapeau"
            AppLanguage.DE -> "Flaggenfarbe Treffen"
            AppLanguage.IT -> "Abbina il Colore della Bandiera"
        }

    val nav_territory_compare: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Bigger Territory"
            AppLanguage.ES -> "Territorio Mayor"
            AppLanguage.FR -> "Plus Grand Territoire"
            AppLanguage.DE -> "Größeres Territorium"
            AppLanguage.IT -> "Territorio Maggiore"
        }

    val nav_true_false: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "True or False"
            AppLanguage.ES -> "Verdadero o Falso"
            AppLanguage.FR -> "Vrai ou Faux"
            AppLanguage.DE -> "Wahr oder Falsch"
            AppLanguage.IT -> "Vero o Falso"
        }

    val nav_blind_map_battle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Blind Map Battle"
            AppLanguage.ES -> "Batalla en el Mapa a Ciegas"
            AppLanguage.FR -> "Bataille sur Carte Aveugle"
            AppLanguage.DE -> "Blinde Kartenschlacht"
            AppLanguage.IT -> "Battaglia sulla Mappa al Buio"
        }

    val no_connection_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "No internet connection"
            AppLanguage.ES -> "Sin conexión a internet"
            AppLanguage.FR -> "Aucune connexion internet"
            AppLanguage.DE -> "Keine Internetverbindung"
            AppLanguage.IT -> "Nessuna connessione internet"
        }

    val results_you_guessed_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You Guessed"
            AppLanguage.ES -> "Adivinaste"
            AppLanguage.FR -> "Tu as Deviné"
            AppLanguage.DE -> "Du hast geraten"
            AppLanguage.IT -> "Hai Indovinato"
        }

    val results_answer_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Answer"
            AppLanguage.ES -> "Respuesta"
            AppLanguage.FR -> "Réponse"
            AppLanguage.DE -> "Antwort"
            AppLanguage.IT -> "Risposta"
        }

    val results_you_landed_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You Landed"
            AppLanguage.ES -> "Quedaste"
            AppLanguage.FR -> "Tu es Tombé"
            AppLanguage.DE -> "Du bist gelandet"
            AppLanguage.IT -> "Sei Arrivato"
        }

    val results_how_everyone_guessed_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "How Everyone Guessed"
            AppLanguage.ES -> "Cómo Adivinó Todo el Mundo"
            AppLanguage.FR -> "Comment Tout le Monde a Deviné"
            AppLanguage.DE -> "Wie alle geraten haben"
            AppLanguage.IT -> "Come Hanno Indovinato Tutti"
        }

    @Composable
    fun results_closer_than_percent(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "closer than %1\$s% of players"
            AppLanguage.ES -> "más cerca que el %1\$s% de los jugadores"
            AppLanguage.FR -> "plus proche que %1\$s% des joueurs"
            AppLanguage.DE -> "näher dran als %1\$s% der Spieler"
            AppLanguage.IT -> "più vicino del %1\$s% dei giocatori"
        }
        return formatTemplate(template, arg1)
    }

    val results_landed_exact: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Nailed It!"
            AppLanguage.ES -> "¡En el Blanco!"
            AppLanguage.FR -> "Dans le Mille !"
            AppLanguage.DE -> "Volltreffer!"
            AppLanguage.IT -> "Centrato!"
        }

    val results_you_chip: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You"
            AppLanguage.ES -> "Tú"
            AppLanguage.FR -> "Toi"
            AppLanguage.DE -> "Du"
            AppLanguage.IT -> "Tu"
        }

    val no_connection_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Today's quizzes need a connection to load — connect to Wi-Fi or mobile data and this screen will move on by itself."
            AppLanguage.ES -> "Los cuestionarios de hoy necesitan conexión para cargarse — conéctate a Wi-Fi o datos móviles y esta pantalla avanzará sola."
            AppLanguage.FR -> "Les quiz du jour ont besoin d'une connexion pour se charger — connecte-toi au Wi-Fi ou aux données mobiles et cet écran passera automatiquement."
            AppLanguage.DE -> "Die heutigen Quizze brauchen eine Verbindung zum Laden — verbinde dich mit WLAN oder mobilen Daten, dann geht es hier von selbst weiter."
            AppLanguage.IT -> "I quiz di oggi hanno bisogno di una connessione per caricarsi — collegati al Wi-Fi o ai dati mobili e questa schermata proseguirà da sola."
        }

}
