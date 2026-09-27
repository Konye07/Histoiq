package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable

object StringsTutorial {
    val tutorial_skip: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Skip"
            AppLanguage.ES -> "Omitir"
            AppLanguage.FR -> "Passer"
            AppLanguage.DE -> "Überspringen"
            AppLanguage.IT -> "Salta"
        }

    val tutorial_next: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Next"
            AppLanguage.ES -> "Siguiente"
            AppLanguage.FR -> "Suivant"
            AppLanguage.DE -> "Weiter"
            AppLanguage.IT -> "Avanti"
        }

    val tutorial_back: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back"
            AppLanguage.ES -> "Atrás"
            AppLanguage.FR -> "Retour"
            AppLanguage.DE -> "Zurück"
            AppLanguage.IT -> "Indietro"
        }

    val tutorial_get_started: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Get Started"
            AppLanguage.ES -> "Empezar"
            AppLanguage.FR -> "Commencer"
            AppLanguage.DE -> "Loslegen"
            AppLanguage.IT -> "Inizia"
        }

    val tutorial_welcome_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Welcome to Historiq"
            AppLanguage.ES -> "Bienvenido a Historiq"
            AppLanguage.FR -> "Bienvenue sur Historiq"
            AppLanguage.DE -> "Willkommen bei Historiq"
            AppLanguage.IT -> "Benvenuto su Historiq"
        }

    val tutorial_welcome_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Six daily history quizzes, each playable once a day and resumable if you step away. Let's take a quick look at each one."
            AppLanguage.ES -> "Seis cuestionarios históricos diarios, cada uno jugable una vez al día y reanudable si te alejas. Echemos un vistazo rápido a cada uno."
            AppLanguage.FR -> "Six quiz d'histoire quotidiens, chacun jouable une fois par jour et reprenable si tu t'absentes. Jetons un coup d'œil rapide à chacun."
            AppLanguage.DE -> "Sechs tägliche Geschichtsquiz, jedes einmal pro Tag spielbar und fortsetzbar, falls du unterbrichst. Werfen wir einen kurzen Blick auf jedes."
            AppLanguage.IT -> "Sei quiz di storia giornalieri, ciascuno giocabile una volta al giorno e ripristinabile se ti interrompi. Diamo una rapida occhiata a ciascuno."
        }

    val tutorial_year_guess_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Read the clue, then guess the year it happened — the closer your guess, the higher your score."
            AppLanguage.ES -> "Lee la pista y adivina el año en que ocurrió — cuanto más te acerques, mayor será tu puntuación."
            AppLanguage.FR -> "Lis l'indice, puis devine l'année où c'est arrivé — plus ta réponse est proche, plus ton score est élevé."
            AppLanguage.DE -> "Lies den Hinweis und rate das Jahr, in dem es geschah — je näher deine Schätzung, desto höher deine Punktzahl."
            AppLanguage.IT -> "Leggi l'indizio e indovina l'anno in cui è successo — più ti avvicini, più alto sarà il tuo punteggio."
        }

    val tutorial_casualties_multiplier_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "See a real battle and guess how many casualties it caused — your score is based on how close your guess is to the real number."
            AppLanguage.ES -> "Observa una batalla real y adivina cuántas bajas causó — tu puntuación depende de cuánto te acerques al número real."
            AppLanguage.FR -> "Découvre une vraie bataille et devine le nombre de pertes qu'elle a causées — ton score dépend de la précision de ta réponse."
            AppLanguage.DE -> "Sieh dir eine echte Schlacht an und schätze die Zahl der Opfer — deine Punktzahl hängt davon ab, wie nah du an der echten Zahl liegst."
            AppLanguage.IT -> "Osserva una battaglia reale e indovina quante vittime ha causato — il tuo punteggio dipende da quanto ti avvicini al numero reale."
        }

    val tutorial_flag_color_match_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "A historical flag has lost its color — drag the sliders to recreate its true hue, saturation, and brightness."
            AppLanguage.ES -> "Una bandera histórica ha perdido su color — mueve los controles para recrear su tono, saturación y brillo reales."
            AppLanguage.FR -> "Un drapeau historique a perdu sa couleur — utilise les curseurs pour recréer sa teinte, sa saturation et sa luminosité réelles."
            AppLanguage.DE -> "Eine historische Flagge hat ihre Farbe verloren — verschiebe die Regler, um ihren echten Farbton, ihre Sättigung und Helligkeit wiederherzustellen."
            AppLanguage.IT -> "Una bandiera storica ha perso il colore — usa i cursori per ricreare la sua vera tonalità, saturazione e luminosità."
        }

    val tutorial_territory_compare_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Pick whichever empire or country controlled more territory at its peak — keep guessing right to survive as many rounds as you can."
            AppLanguage.ES -> "Elige el imperio o país que controló más territorio en su apogeo — sigue acertando para sobrevivir el mayor número de rondas posible."
            AppLanguage.FR -> "Choisis l'empire ou le pays qui contrôlait le plus de territoire à son apogée — continue à deviner juste pour survivre le plus longtemps possible."
            AppLanguage.DE -> "Wähle das Reich oder Land, das auf seinem Höhepunkt das größere Gebiet kontrollierte — errate weiter richtig, um so viele Runden wie möglich zu überstehen."
            AppLanguage.IT -> "Scegli l'impero o il paese che controllava più territorio al suo apice — continua a indovinare per sopravvivere più round possibile."
        }

    val tutorial_true_false_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Decide whether each historical statement is true or false — one mistake ends the run, so stay sharp across all 10 rounds."
            AppLanguage.ES -> "Decide si cada afirmación histórica es verdadera o falsa — un error termina la partida, así que mantente atento en las 10 rondas."
            AppLanguage.FR -> "Détermine si chaque affirmation historique est vraie ou fausse — une seule erreur met fin à la partie, alors reste concentré sur les 10 manches."
            AppLanguage.DE -> "Entscheide, ob jede historische Aussage wahr oder falsch ist — ein Fehler beendet den Lauf, also bleib über alle 10 Runden konzentriert."
            AppLanguage.IT -> "Decidi se ogni affermazione storica è vera o falsa — un errore termina la partita, quindi resta concentrato per tutti i 10 round."
        }

    val tutorial_blind_map_battle_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Pinch to zoom, drag to pan, and tap the blank map to pinpoint exactly where a historical battle took place."
            AppLanguage.ES -> "Pellizca para hacer zoom, arrastra para desplazarte y toca el mapa en blanco para marcar dónde ocurrió exactamente una batalla histórica."
            AppLanguage.FR -> "Pince pour zoomer, glisse pour te déplacer, et touche la carte vierge pour indiquer précisément où une bataille historique a eu lieu."
            AppLanguage.DE -> "Zoome mit zwei Fingern, ziehe zum Verschieben und tippe auf die leere Karte, um genau zu markieren, wo eine historische Schlacht stattfand."
            AppLanguage.IT -> "Pizzica per ingrandire, trascina per spostarti e tocca la mappa vuota per indicare esattamente dove si è svolta una battaglia storica."
        }

    val tutorial_closing_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You're All Set"
            AppLanguage.ES -> "Todo Listo"
            AppLanguage.FR -> "Tout Est Prêt"
            AppLanguage.DE -> "Bereit Zum Start"
            AppLanguage.IT -> "Tutto Pronto"
        }

    val tutorial_closing_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "A new round unlocks for each game every day — come back daily to keep your streak alive."
            AppLanguage.ES -> "Cada día se desbloquea una nueva ronda para cada juego — vuelve a diario para mantener viva tu racha."
            AppLanguage.FR -> "Une nouvelle manche se débloque chaque jour pour chaque jeu — reviens tous les jours pour garder ta série en vie."
            AppLanguage.DE -> "Jeden Tag wird für jedes Spiel eine neue Runde freigeschaltet — komm täglich zurück, um deine Serie am Leben zu erhalten."
            AppLanguage.IT -> "Ogni giorno si sblocca un nuovo round per ogni gioco — torna ogni giorno per mantenere viva la tua serie."
        }
}
