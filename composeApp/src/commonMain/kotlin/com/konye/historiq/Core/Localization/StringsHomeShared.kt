package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable

object StringsHomeShared {
    val home_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Historiq"
            AppLanguage.ES -> "Historiq"
            AppLanguage.FR -> "Historiq"
            AppLanguage.DE -> "Historiq"
            AppLanguage.IT -> "Historiq"
            AppLanguage.HU -> "Historiq"
        }

    val home_desc_year_guess: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "An event is given — guess the year it happened. The closer you are, the more points you score."
            AppLanguage.ES -> "Se muestra un evento: adivina el año en que ocurrió. Cuanto más te acerques, más puntos consigues."
            AppLanguage.FR -> "Un événement est donné — devine l'année à laquelle il a eu lieu. Plus tu es proche, plus tu marques de points."
            AppLanguage.DE -> "Ein Ereignis wird gezeigt — errate, in welchem Jahr es stattfand. Je näher du liegst, desto mehr Punkte bekommst du."
            AppLanguage.IT -> "Viene mostrato un evento: indovina l'anno in cui è accaduto. Più ti avvicini, più punti ottieni."
            AppLanguage.HU -> "Kapsz egy eseményt — találd ki, melyik évben történt. Minél közelebb jársz, annál több pontot kapsz."
        }

    val home_desc_casualties_multiplier: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "A battle is given — guess the casualties. The closer your guess, the smaller your multiplier. The goal is the lowest multiplier you can get."
            AppLanguage.ES -> "Se muestra una batalla: adivina el número de bajas. Cuanto más te acerques, menor será tu multiplicador. El objetivo es conseguir el multiplicador más bajo posible."
            AppLanguage.FR -> "Une bataille est donnée — devine le nombre de pertes. Plus ton estimation est proche, plus ton multiplicateur est bas. L'objectif est d'obtenir le multiplicateur le plus bas possible."
            AppLanguage.DE -> "Eine Schlacht wird gezeigt — schätze die Verluste. Je näher deine Schätzung ist, desto kleiner dein Multiplikator. Ziel ist der niedrigstmögliche Multiplikator."
            AppLanguage.IT -> "Viene mostrata una battaglia: indovina il numero delle vittime. Più la tua stima è vicina, minore sarà il tuo moltiplicatore. L'obiettivo è ottenere il moltiplicatore più basso possibile."
            AppLanguage.HU -> "Kapsz egy csatát — találd ki a veszteségek számát. Minél közelebb a tipped, annál kisebb a szorzód. A cél a lehető legalacsonyabb szorzó elérése."
        }

    val home_desc_flag_color_match: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Match a historical flag's true color using hue, saturation and brightness sliders."
            AppLanguage.ES -> "Iguala el color real de una bandera histórica ajustando los deslizadores de tono, saturación y brillo."
            AppLanguage.FR -> "Reproduis la couleur réelle d'un drapeau historique à l'aide des curseurs de teinte, saturation et luminosité."
            AppLanguage.DE -> "Triff die wahre Farbe einer historischen Flagge mit den Reglern für Farbton, Sättigung und Helligkeit."
            AppLanguage.IT -> "Abbina il vero colore di una bandiera storica usando i cursori di tonalità, saturazione e luminosità."
            AppLanguage.HU -> "Találd el egy történelmi zászló valódi színét az árnyalat-, telítettség- és fényerő-csúszkákkal."
        }

    val home_desc_territory_compare: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Pick which empire had the bigger territory at its prime. One wrong pick ends the run."
            AppLanguage.ES -> "Elige qué imperio tuvo el mayor territorio en su apogeo. Un solo fallo termina la partida."
            AppLanguage.FR -> "Choisis quel empire avait le plus grand territoire à son apogée. Une seule erreur met fin à la partie."
            AppLanguage.DE -> "Wähle, welches Reich auf seinem Höhepunkt das größere Territorium hatte. Eine falsche Wahl beendet den Lauf."
            AppLanguage.IT -> "Scegli quale impero avesse il territorio più vasto al suo apice. Una sola scelta sbagliata termina la partita."
            AppLanguage.HU -> "Válaszd ki, melyik birodalomnak volt nagyobb területe a fénykorában. Egyetlen rossz tipp véget vet a menetnek."
        }

    val home_desc_true_false: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Historical statements — one wrong answer ends the run."
            AppLanguage.ES -> "Afirmaciones históricas: una sola respuesta incorrecta termina la partida."
            AppLanguage.FR -> "Des affirmations historiques — une seule mauvaise réponse met fin à la partie."
            AppLanguage.DE -> "Historische Aussagen — eine falsche Antwort beendet den Lauf."
            AppLanguage.IT -> "Affermazioni storiche: una sola risposta sbagliata termina la partita."
            AppLanguage.HU -> "Történelmi állítások — egyetlen rossz válasz véget vet a menetnek."
        }

    val home_desc_blind_map_battle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Place a historical battle on the map with no borders shown. The closer your pin, the more points."
            AppLanguage.ES -> "Marca en el mapa la ubicación de una batalla histórica sin fronteras visibles. Cuanto más cerca coloques tu marcador, más puntos consigues."
            AppLanguage.FR -> "Place une bataille historique sur la carte, sans aucune frontière affichée. Plus ton repère est précis, plus tu marques de points."
            AppLanguage.DE -> "Markiere den Ort einer historischen Schlacht auf der Karte ohne eingezeichnete Grenzen. Je näher deine Markierung, desto mehr Punkte."
            AppLanguage.IT -> "Posiziona sulla mappa una battaglia storica senza confini visibili. Più il tuo segnaposto è preciso, più punti ottieni."
            AppLanguage.HU -> "Helyezz el egy történelmi csatát a térképen, határok nélkül. Minél közelebb a jelölőd, annál több pontot kapsz."
        }

    val home_coming_soon: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Coming soon"
            AppLanguage.ES -> "Próximamente"
            AppLanguage.FR -> "Bientôt disponible"
            AppLanguage.DE -> "Demnächst verfügbar"
            AppLanguage.IT -> "Prossimamente"
            AppLanguage.HU -> "Hamarosan"
        }

    @Composable
    fun home_about_content_desc(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "About %1\$s"
            AppLanguage.ES -> "Acerca de %1\$s"
            AppLanguage.FR -> "À propos de %1\$s"
            AppLanguage.DE -> "Über %1\$s"
            AppLanguage.IT -> "Informazioni su %1\$s"
            AppLanguage.HU -> "Névjegy: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun home_streak_content_desc(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s-day streak"
            AppLanguage.ES -> "Racha de %1\$s días"
            AppLanguage.FR -> "Série de %1\$s jours"
            AppLanguage.DE -> "%1\$s-Tage-Serie"
            AppLanguage.IT -> "Serie di %1\$s giorni"
            AppLanguage.HU -> "%1\$s napos sorozat"
        }
        return formatTemplate(template, arg1)
    }

    val home_streak_dialog_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Daily Streak"
            AppLanguage.ES -> "Racha diaria"
            AppLanguage.FR -> "Série quotidienne"
            AppLanguage.DE -> "Tages-Serie"
            AppLanguage.IT -> "Serie giornaliera"
            AppLanguage.HU -> "Napi sorozat"
        }

    val home_streak_dialog_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "How many days in a row you've played this game. The torch lights up once today's game is done — it stays gray until then, and the streak resets if you miss a full day."
            AppLanguage.ES -> "Cuántos días seguidos has jugado a este juego. La antorcha se enciende en cuanto completas el juego de hoy; permanece apagada hasta entonces, y la racha se pierde si te saltas un día entero."
            AppLanguage.FR -> "Le nombre de jours consécutifs où tu as joué à ce jeu. La torche s'allume une fois la partie du jour terminée — elle reste éteinte jusque-là, et la série est perdue si tu sautes une journée entière."
            AppLanguage.DE -> "Wie viele Tage in Folge du dieses Spiel gespielt hast. Die Fackel leuchtet, sobald du das heutige Spiel abgeschlossen hast — bis dahin bleibt sie grau, und die Serie reißt ab, wenn du einen ganzen Tag auslässt."
            AppLanguage.IT -> "Per quanti giorni di fila hai giocato a questo gioco. La torcia si accende non appena completi la partita di oggi — resta spenta fino ad allora, e la serie si interrompe se salti un giorno intero."
            AppLanguage.HU -> "Hány napja játszol egyfolytában ezzel a játékkal. A fáklya akkor lobban fel, amint befejezted a mai játékot — addig szürke marad, és a sorozat megszakad, ha egy egész napot kihagysz."
        }

    @Composable
    fun home_info_dialog_title(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s — %2\$s"
            AppLanguage.ES -> "%1\$s — %2\$s"
            AppLanguage.FR -> "%1\$s — %2\$s"
            AppLanguage.DE -> "%1\$s — %2\$s"
            AppLanguage.IT -> "%1\$s — %2\$s"
            AppLanguage.HU -> "%1\$s — %2\$s"
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
            AppLanguage.HU -> "Bezárás"
        }

    val suggestion_panel_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Got a suggestion?"
            AppLanguage.ES -> "¿Tienes alguna sugerencia?"
            AppLanguage.FR -> "Une suggestion ?"
            AppLanguage.DE -> "Hast du einen Vorschlag?"
            AppLanguage.IT -> "Hai un suggerimento?"
            AppLanguage.HU -> "Van egy ötleted?"
        }

    val suggestion_panel_info_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Suggest new game modes, or share any feedback about your experience with the current games."
            AppLanguage.ES -> "Sugiere nuevos modos de juego o comparte cualquier comentario sobre tu experiencia con los juegos actuales."
            AppLanguage.FR -> "Suggère de nouveaux modes de jeu ou partage tes retours sur ton expérience avec les jeux actuels."
            AppLanguage.DE -> "Schlage neue Spielmodi vor oder teile dein Feedback zu deiner Erfahrung mit den aktuellen Spielen."
            AppLanguage.IT -> "Suggerisci nuove modalità di gioco o condividi un feedback sulla tua esperienza con i giochi attuali."
            AppLanguage.HU -> "Javasolj új játékmódokat, vagy oszd meg a véleményedet a jelenlegi játékokkal szerzett élményeidről."
        }

    val suggestion_panel_content_desc: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "About suggestions"
            AppLanguage.ES -> "Acerca de las sugerencias"
            AppLanguage.FR -> "À propos des suggestions"
            AppLanguage.DE -> "Über Vorschläge"
            AppLanguage.IT -> "Informazioni sui suggerimenti"
            AppLanguage.HU -> "Az ötletekről"
        }

    val email_copy_content_desc: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Copy email address"
            AppLanguage.ES -> "Copiar la dirección de correo"
            AppLanguage.FR -> "Copier l'adresse e-mail"
            AppLanguage.DE -> "E-Mail-Adresse kopieren"
            AppLanguage.IT -> "Copia l'indirizzo e-mail"
            AppLanguage.HU -> "E-mail-cím másolása"
        }

    val email_copied: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Email address copied"
            AppLanguage.ES -> "Dirección de correo copiada"
            AppLanguage.FR -> "Adresse e-mail copiée"
            AppLanguage.DE -> "E-Mail-Adresse kopiert"
            AppLanguage.IT -> "Indirizzo e-mail copiato"
            AppLanguage.HU -> "E-mail-cím átmásolva"
        }

    val email_no_mail_app: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "No mail app found — email address copied"
            AppLanguage.ES -> "No se encontró ninguna aplicación de correo — dirección copiada"
            AppLanguage.FR -> "Aucune application de messagerie trouvée — adresse copiée"
            AppLanguage.DE -> "Keine Mail-App gefunden — E-Mail-Adresse kopiert"
            AppLanguage.IT -> "Nessuna app di posta trovata — indirizzo copiato"
            AppLanguage.HU -> "Nem található levelezőalkalmazás — az e-mail-cím átmásolva"
        }

    @Composable
    fun home_countdown(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Test your knowledge again in %1\$s"
            AppLanguage.ES -> "Pon a prueba tus conocimientos de nuevo en %1\$s"
            AppLanguage.FR -> "Teste à nouveau tes connaissances dans %1\$s"
            AppLanguage.DE -> "Teste dein Wissen erneut in %1\$s"
            AppLanguage.IT -> "Metti di nuovo alla prova le tue conoscenze tra %1\$s"
            AppLanguage.HU -> "Tedd újra próbára a tudásodat %1\$s múlva"
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
            AppLanguage.HU -> "Végeredmény"
        }

    val results_rank_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Rank"
            AppLanguage.ES -> "Puesto"
            AppLanguage.FR -> "Classement"
            AppLanguage.DE -> "Platz"
            AppLanguage.IT -> "Posizione"
            AppLanguage.HU -> "Helyezés"
        }

    @Composable
    fun results_of_points_max(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "of %1\$s points"
            AppLanguage.ES -> "de %1\$s puntos"
            AppLanguage.FR -> "sur %1\$s points"
            AppLanguage.DE -> "von %1\$s Punkten"
            AppLanguage.IT -> "su %1\$s punti"
            AppLanguage.HU -> "%1\$s pontból"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun results_of_players_total(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "of %1\$s players"
            AppLanguage.ES -> "de %1\$s jugadores"
            AppLanguage.FR -> "sur %1\$s joueurs"
            AppLanguage.DE -> "von %1\$s Spielern"
            AppLanguage.IT -> "su %1\$s giocatori"
            AppLanguage.HU -> "%1\$s játékosból"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun results_better_than_percent(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Better than %1\$s%"
            AppLanguage.ES -> "Mejor que el %1\$s%"
            AppLanguage.FR -> "Meilleur que %1\$s%"
            AppLanguage.DE -> "Besser als %1\$s%"
            AppLanguage.IT -> "Meglio del %1\$s%"
            AppLanguage.HU -> "Jobb, mint a játékosok %1\$s%-a"
        }
        return formatTemplate(template, arg1)
    }

    val results_share: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Share Results"
            AppLanguage.ES -> "Compartir Resultados"
            AppLanguage.FR -> "Partager les Résultats"
            AppLanguage.DE -> "Ergebnisse Teilen"
            AppLanguage.IT -> "Condividi Risultati"
            AppLanguage.HU -> "Eredmények megosztása"
        }

    val results_share_chooser_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Share your results"
            AppLanguage.ES -> "Comparte tus resultados"
            AppLanguage.FR -> "Partage tes résultats"
            AppLanguage.DE -> "Teile deine Ergebnisse"
            AppLanguage.IT -> "Condividi i tuoi risultati"
            AppLanguage.HU -> "Oszd meg az eredményeidet"
        }

    @Composable
    fun results_share_message(arg1: Any, arg2: Any, arg3: Any, arg4: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "I scored %1\$s in %2\$s on %3\$s! Can you beat me?\\n\\n%4\$s"
            AppLanguage.ES -> "¡Saqué %1\$s en %2\$s en %3\$s! ¿Puedes superarme?\\n\\n%4\$s"
            AppLanguage.FR -> "J'ai obtenu %1\$s en %2\$s sur %3\$s ! Peux-tu me battre ?\\n\\n%4\$s"
            AppLanguage.DE -> "Ich habe %1\$s in %2\$s bei %3\$s erreicht! Schaffst du mehr?\\n\\n%4\$s"
            AppLanguage.IT -> "Ho ottenuto %1\$s in %2\$s su %3\$s! Riesci a battermi?\\n\\n%4\$s"
            AppLanguage.HU -> "%1\$s pontot értem el a(z) %2\$s játékban a(z) %3\$s alkalmazásban! Meg tudsz verni?\\n\\n%4\$s"
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
            AppLanguage.HU -> "Meg tudsz verni? Játssz te is: %1\$s"
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
            AppLanguage.HU -> "Tipp"
        }

    val hint_dialog_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Want a hint?"
            AppLanguage.ES -> "¿Quieres una pista?"
            AppLanguage.FR -> "Tu veux un indice ?"
            AppLanguage.DE -> "Möchtest du einen Hinweis?"
            AppLanguage.IT -> "Vuoi un suggerimento?"
            AppLanguage.HU -> "Kérsz egy tippet?"
        }

    val hint_dialog_text: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Watch a short ad to find out which war or conflict this is part of."
            AppLanguage.ES -> "Mira un breve anuncio para descubrir a qué guerra o conflicto pertenece esto."
            AppLanguage.FR -> "Regarde une courte publicité pour découvrir à quelle guerre ou quel conflit cela appartient."
            AppLanguage.DE -> "Sieh dir eine kurze Werbung an, um herauszufinden, zu welchem Krieg oder Konflikt dies gehört."
            AppLanguage.IT -> "Guarda un breve annuncio per scoprire a quale guerra o conflitto appartiene."
            AppLanguage.HU -> "Nézz meg egy rövid hirdetést, hogy megtudd, melyik háborúhoz vagy konfliktushoz tartozik ez."
        }

    val hint_watch_ad: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Watch Ad"
            AppLanguage.ES -> "Ver Anuncio"
            AppLanguage.FR -> "Voir la Pub"
            AppLanguage.DE -> "Werbung Ansehen"
            AppLanguage.IT -> "Guarda l'Annuncio"
            AppLanguage.HU -> "Hirdetés megtekintése"
        }

    val hint_cancel: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Cancel"
            AppLanguage.ES -> "Cancelar"
            AppLanguage.FR -> "Annuler"
            AppLanguage.DE -> "Abbrechen"
            AppLanguage.IT -> "Annulla"
            AppLanguage.HU -> "Mégse"
        }

    val nav_play: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Play"
            AppLanguage.ES -> "Jugar"
            AppLanguage.FR -> "Jouer"
            AppLanguage.DE -> "Spielen"
            AppLanguage.IT -> "Gioca"
            AppLanguage.HU -> "Játék"
        }

    val nav_friends: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Friends"
            AppLanguage.ES -> "Amigos"
            AppLanguage.FR -> "Amis"
            AppLanguage.DE -> "Freunde"
            AppLanguage.IT -> "Amici"
            AppLanguage.HU -> "Barátok"
        }

    val nav_profile: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Profile"
            AppLanguage.ES -> "Perfil"
            AppLanguage.FR -> "Profil"
            AppLanguage.DE -> "Profil"
            AppLanguage.IT -> "Profilo"
            AppLanguage.HU -> "Profil"
        }

    val nav_settings: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Settings"
            AppLanguage.ES -> "Ajustes"
            AppLanguage.FR -> "Paramètres"
            AppLanguage.DE -> "Einstellungen"
            AppLanguage.IT -> "Impostazioni"
            AppLanguage.HU -> "Beállítások"
        }

    val nav_year_guess: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Guess the Year"
            AppLanguage.ES -> "Adivina el Año"
            AppLanguage.FR -> "Devine l'Année"
            AppLanguage.DE -> "Errate das Jahr"
            AppLanguage.IT -> "Indovina l'Anno"
            AppLanguage.HU -> "Találd ki az évet"
        }

    val nav_casualties_multiplier: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Casualty Count"
            AppLanguage.ES -> "Cuenta de Bajas"
            AppLanguage.FR -> "Compte des Pertes"
            AppLanguage.DE -> "Verlustzahlen"
            AppLanguage.IT -> "Conta delle Vittime"
            AppLanguage.HU -> "Veszteségek száma"
        }

    val nav_flag_color_match: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Match the Flag Color"
            AppLanguage.ES -> "Iguala el Color de la Bandera"
            AppLanguage.FR -> "Reproduis la Couleur du Drapeau"
            AppLanguage.DE -> "Flaggenfarbe Treffen"
            AppLanguage.IT -> "Abbina il Colore della Bandiera"
            AppLanguage.HU -> "Találd el a zászló színét"
        }

    val nav_territory_compare: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Bigger Territory"
            AppLanguage.ES -> "Territorio Mayor"
            AppLanguage.FR -> "Plus Grand Territoire"
            AppLanguage.DE -> "Größeres Territorium"
            AppLanguage.IT -> "Territorio Maggiore"
            AppLanguage.HU -> "Nagyobb terület"
        }

    val nav_true_false: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "True or False"
            AppLanguage.ES -> "Verdadero o Falso"
            AppLanguage.FR -> "Vrai ou Faux"
            AppLanguage.DE -> "Wahr oder Falsch"
            AppLanguage.IT -> "Vero o Falso"
            AppLanguage.HU -> "Igaz vagy hamis"
        }

    val nav_blind_map_battle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Blind Map Battle"
            AppLanguage.ES -> "Batalla en el Mapa a Ciegas"
            AppLanguage.FR -> "Bataille sur Carte Aveugle"
            AppLanguage.DE -> "Blinde Kartenschlacht"
            AppLanguage.IT -> "Battaglia sulla Mappa al Buio"
            AppLanguage.HU -> "Vak térképcsata"
        }

    val results_you_guessed_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You Guessed"
            AppLanguage.ES -> "Adivinaste"
            AppLanguage.FR -> "Tu as Deviné"
            AppLanguage.DE -> "Du hast geraten"
            AppLanguage.IT -> "Hai Indovinato"
            AppLanguage.HU -> "A tipped"
        }

    val results_answer_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Answer"
            AppLanguage.ES -> "Respuesta"
            AppLanguage.FR -> "Réponse"
            AppLanguage.DE -> "Antwort"
            AppLanguage.IT -> "Risposta"
            AppLanguage.HU -> "Válasz"
        }

    val results_you_landed_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You Landed"
            AppLanguage.ES -> "Quedaste"
            AppLanguage.FR -> "Tu es Tombé"
            AppLanguage.DE -> "Du bist gelandet"
            AppLanguage.IT -> "Sei Arrivato"
            AppLanguage.HU -> "Ide jutottál"
        }

    val results_why_it_matters_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Why It Matters"
            AppLanguage.ES -> "Por Qué Importa"
            AppLanguage.FR -> "Pourquoi c'est Important"
            AppLanguage.DE -> "Warum es wichtig ist"
            AppLanguage.IT -> "Perché è Importante"
            AppLanguage.HU -> "Miért fontos"
        }

    val results_how_everyone_guessed_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "How Everyone Guessed"
            AppLanguage.ES -> "Cómo Adivinó Todo el Mundo"
            AppLanguage.FR -> "Comment Tout le Monde a Deviné"
            AppLanguage.DE -> "Wie alle geraten haben"
            AppLanguage.IT -> "Come Hanno Indovinato Tutti"
            AppLanguage.HU -> "Hogyan tippelt mindenki"
        }

    @Composable
    fun results_closer_than_percent(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "closer than %1\$s% of players"
            AppLanguage.ES -> "más cerca que el %1\$s% de los jugadores"
            AppLanguage.FR -> "plus proche que %1\$s% des joueurs"
            AppLanguage.DE -> "näher dran als %1\$s% der Spieler"
            AppLanguage.IT -> "più vicino del %1\$s% dei giocatori"
            AppLanguage.HU -> "közelebb, mint a játékosok %1\$s%-a"
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
            AppLanguage.HU -> "Telitalálat!"
        }

    val results_you_chip: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You"
            AppLanguage.ES -> "Tú"
            AppLanguage.FR -> "Toi"
            AppLanguage.DE -> "Du"
            AppLanguage.IT -> "Tu"
            AppLanguage.HU -> "Te"
        }

    val offline_banner_message: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "No stable internet connection — games, stats and friends can't load until you're back online."
            AppLanguage.ES -> "Sin conexión estable a internet — los juegos, las estadísticas y los amigos no se cargarán hasta que vuelvas a estar en línea."
            AppLanguage.FR -> "Pas de connexion internet stable — les jeux, les statistiques et les amis ne se chargeront pas tant que tu n'es pas reconnecté."
            AppLanguage.DE -> "Keine stabile Internetverbindung — Spiele, Statistiken und Freunde laden erst wieder, wenn du online bist."
            AppLanguage.IT -> "Nessuna connessione internet stabile — giochi, statistiche e amici non si caricheranno finché non sei di nuovo online."
            AppLanguage.HU -> "Nincs stabil internetkapcsolat — a játékok, a statisztikák és a barátok csak akkor töltődnek be, ha újra online vagy."
        }

}
