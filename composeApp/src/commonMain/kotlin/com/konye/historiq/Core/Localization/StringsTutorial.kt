package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable

object StringsTutorial {
    val language_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Choose your language"
            AppLanguage.ES -> "Elige tu idioma"
            AppLanguage.FR -> "Choisis ta langue"
            AppLanguage.DE -> "Wähle deine Sprache"
            AppLanguage.IT -> "Scegli la tua lingua"
            AppLanguage.HU -> "Válaszd ki a nyelvedet"
        }

    /** [arg1] is the name of the Settings tab, where the language can be changed later. */
    @Composable
    fun language_subtitle(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "The tutorial and the whole app will use it. You can change it any time under %1\$s."
            AppLanguage.ES -> "El tutorial y toda la aplicación la usarán. Puedes cambiarla cuando quieras en %1\$s."
            AppLanguage.FR -> "Le tutoriel et toute l'application l'utiliseront. Tu peux la changer à tout moment dans %1\$s."
            AppLanguage.DE -> "Das Tutorial und die ganze App verwenden sie. Du kannst sie jederzeit unter „%1\$s“ ändern."
            AppLanguage.IT -> "Il tutorial e tutta l'app la useranno. Puoi cambiarla in qualsiasi momento in %1\$s."
            AppLanguage.HU -> "A bemutató és az egész alkalmazás ezt fogja használni. Bármikor megváltoztathatod a(z) %1\$s menüben."
        }
        return formatTemplate(template, arg1)
    }

    val language_continue: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Continue"
            AppLanguage.ES -> "Continuar"
            AppLanguage.FR -> "Continuer"
            AppLanguage.DE -> "Weiter"
            AppLanguage.IT -> "Continua"
            AppLanguage.HU -> "Folytatás"
        }

    val tutorial_skip: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Skip"
            AppLanguage.ES -> "Omitir"
            AppLanguage.FR -> "Passer"
            AppLanguage.DE -> "Überspringen"
            AppLanguage.IT -> "Salta"
            AppLanguage.HU -> "Kihagyás"
        }

    val tutorial_next: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Next"
            AppLanguage.ES -> "Siguiente"
            AppLanguage.FR -> "Suivant"
            AppLanguage.DE -> "Weiter"
            AppLanguage.IT -> "Avanti"
            AppLanguage.HU -> "Tovább"
        }

    val tutorial_back: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Back"
            AppLanguage.ES -> "Atrás"
            AppLanguage.FR -> "Retour"
            AppLanguage.DE -> "Zurück"
            AppLanguage.IT -> "Indietro"
            AppLanguage.HU -> "Vissza"
        }

    val tutorial_get_started: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Get Started"
            AppLanguage.ES -> "Empezar"
            AppLanguage.FR -> "Commencer"
            AppLanguage.DE -> "Loslegen"
            AppLanguage.IT -> "Inizia"
            AppLanguage.HU -> "Kezdjük el"
        }

    val tutorial_welcome_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Welcome to Historiq"
            AppLanguage.ES -> "Bienvenido a Historiq"
            AppLanguage.FR -> "Bienvenue sur Historiq"
            AppLanguage.DE -> "Willkommen bei Historiq"
            AppLanguage.IT -> "Benvenuto su Historiq"
            AppLanguage.HU -> "Üdvözöl a Historiq"
        }

    val tutorial_welcome_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Six daily history quizzes, each playable once a day and resumable if you step away — plus five historical eras to step into and a Daily Battle that brings all six together. Let's take a quick look."
            AppLanguage.ES -> "Seis cuestionarios históricos diarios, cada uno jugable una vez al día y reanudable si te alejas — además de cinco épocas históricas en las que sumergirte y una Batalla Diaria que reúne los seis. Echemos un vistazo rápido."
            AppLanguage.FR -> "Six quiz d'histoire quotidiens, chacun jouable une fois par jour et reprenable si tu t'absentes — plus cinq époques historiques à explorer et une Bataille du Jour qui réunit les six. Jetons un coup d'œil rapide."
            AppLanguage.DE -> "Sechs tägliche Geschichtsquiz, jedes einmal pro Tag spielbar und fortsetzbar, falls du unterbrichst — dazu fünf historische Epochen zum Eintauchen und eine Tagesschlacht, die alle sechs vereint. Werfen wir einen kurzen Blick darauf."
            AppLanguage.IT -> "Sei quiz di storia giornalieri, ciascuno giocabile una volta al giorno e ripristinabile se ti interrompi — più cinque epoche storiche in cui immergerti e una Battaglia del Giorno che li riunisce tutti e sei. Diamo una rapida occhiata."
            AppLanguage.HU -> "Hat napi történelmi kvíz, mindegyik naponta egyszer játszható, és folytatható, ha megszakítod — plusz öt történelmi korszak, amelybe belemerülhetsz, és egy Napi Csata, amely mind a hatot egyesíti. Vessünk rájuk egy gyors pillantást."
        }

    val tutorial_year_guess_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Read the clue, then guess the year it happened — the closer your guess, the higher your score."
            AppLanguage.ES -> "Lee la pista y adivina el año en que ocurrió — cuanto más te acerques, mayor será tu puntuación."
            AppLanguage.FR -> "Lis l'indice, puis devine l'année où c'est arrivé — plus ta réponse est proche, plus ton score est élevé."
            AppLanguage.DE -> "Lies den Hinweis und rate das Jahr, in dem es geschah — je näher deine Schätzung, desto höher deine Punktzahl."
            AppLanguage.IT -> "Leggi l'indizio e indovina l'anno in cui è successo — più ti avvicini, più alto sarà il tuo punteggio."
            AppLanguage.HU -> "Olvasd el a támpontot, és találd ki, melyik évben történt — minél közelebb jársz, annál magasabb a pontszámod."
        }

    val tutorial_casualties_multiplier_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "See a real battle and guess how many casualties it caused — your score is based on how close your guess is to the real number."
            AppLanguage.ES -> "Observa una batalla real y adivina cuántas bajas causó — tu puntuación depende de cuánto te acerques al número real."
            AppLanguage.FR -> "Découvre une vraie bataille et devine le nombre de pertes qu'elle a causées — ton score dépend de la précision de ta réponse."
            AppLanguage.DE -> "Sieh dir eine echte Schlacht an und schätze die Zahl der Opfer — deine Punktzahl hängt davon ab, wie nah du an der echten Zahl liegst."
            AppLanguage.IT -> "Osserva una battaglia reale e indovina quante vittime ha causato — il tuo punteggio dipende da quanto ti avvicini al numero reale."
            AppLanguage.HU -> "Nézz meg egy valódi csatát, és találd ki, hány áldozatot követelt — a pontszámod azon múlik, mennyire közelíted meg a valós számot."
        }

    val tutorial_flag_color_match_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "A historical flag has lost its color — drag the sliders to recreate its true hue, saturation, and brightness."
            AppLanguage.ES -> "Una bandera histórica ha perdido su color — mueve los controles para recrear su tono, saturación y brillo reales."
            AppLanguage.FR -> "Un drapeau historique a perdu sa couleur — utilise les curseurs pour recréer sa teinte, sa saturation et sa luminosité réelles."
            AppLanguage.DE -> "Eine historische Flagge hat ihre Farbe verloren — verschiebe die Regler, um ihren echten Farbton, ihre Sättigung und Helligkeit wiederherzustellen."
            AppLanguage.IT -> "Una bandiera storica ha perso il colore — usa i cursori per ricreare la sua vera tonalità, saturazione e luminosità."
            AppLanguage.HU -> "Egy történelmi zászló elvesztette a színét — húzd a csúszkákat, hogy visszaállítsd a valódi árnyalatát, telítettségét és fényerejét."
        }

    val tutorial_territory_compare_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Pick whichever empire or country controlled more territory at its peak — keep guessing right to survive as many rounds as you can."
            AppLanguage.ES -> "Elige el imperio o país que controló más territorio en su apogeo — sigue acertando para sobrevivir el mayor número de rondas posible."
            AppLanguage.FR -> "Choisis l'empire ou le pays qui contrôlait le plus de territoire à son apogée — continue à deviner juste pour survivre le plus longtemps possible."
            AppLanguage.DE -> "Wähle das Reich oder Land, das auf seinem Höhepunkt das größere Gebiet kontrollierte — errate weiter richtig, um so viele Runden wie möglich zu überstehen."
            AppLanguage.IT -> "Scegli l'impero o il paese che controllava più territorio al suo apice — continua a indovinare per sopravvivere più round possibile."
            AppLanguage.HU -> "Válaszd ki, melyik birodalom vagy ország uralt nagyobb területet a csúcspontján — találj el minél többet, hogy minél több fordulót túlélj."
        }

    val tutorial_true_false_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Decide whether each historical statement is true or false — one mistake ends the run, so stay sharp across all 10 rounds."
            AppLanguage.ES -> "Decide si cada afirmación histórica es verdadera o falsa — un error termina la partida, así que mantente atento en las 10 rondas."
            AppLanguage.FR -> "Détermine si chaque affirmation historique est vraie ou fausse — une seule erreur met fin à la partie, alors reste concentré sur les 10 manches."
            AppLanguage.DE -> "Entscheide, ob jede historische Aussage wahr oder falsch ist — ein Fehler beendet den Lauf, also bleib über alle 10 Runden konzentriert."
            AppLanguage.IT -> "Decidi se ogni affermazione storica è vera o falsa — un errore termina la partita, quindi resta concentrato per tutti i 10 round."
            AppLanguage.HU -> "Döntsd el, igaz vagy hamis-e az adott történelmi állítás — egyetlen hiba véget vet a menetnek, ezért maradj éber mind a 10 fordulóban."
        }

    val tutorial_blind_map_battle_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Pinch to zoom, drag to pan, and tap the blank map to pinpoint exactly where a historical battle took place."
            AppLanguage.ES -> "Pellizca para hacer zoom, arrastra para desplazarte y toca el mapa en blanco para marcar dónde ocurrió exactamente una batalla histórica."
            AppLanguage.FR -> "Pince pour zoomer, glisse pour te déplacer, et touche la carte vierge pour indiquer précisément où une bataille historique a eu lieu."
            AppLanguage.DE -> "Zoome mit zwei Fingern, ziehe zum Verschieben und tippe auf die leere Karte, um genau zu markieren, wo eine historische Schlacht stattfand."
            AppLanguage.IT -> "Pizzica per ingrandire, trascina per spostarti e tocca la mappa vuota per indicare esattamente dove si è svolta una battaglia storica."
            AppLanguage.HU -> "Csippentéssel nagyíts, húzással mozgasd a térképet, és koppints az üres térképre, hogy pontosan megjelöld, hol zajlott egy történelmi csata."
        }

    /** [arg1] is the most a Daily Battle can score, already formatted ("6 000"), [arg2] the name of the Play tab. */
    @Composable
    fun tutorial_battle_body(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "One question from each of the six games, one after the other. Your six answers add up to a single score out of %1\$s, ranked against everyone who plays that day. You'll find it at the top of the %2\$s screen."
            AppLanguage.ES -> "Una pregunta de cada uno de los seis juegos, una tras otra. Tus seis respuestas suman una única puntuación sobre %1\$s, clasificada frente a todos los que juegan ese día. La encontrarás arriba en la pantalla %2\$s."
            AppLanguage.FR -> "Une question de chacun des six jeux, l'une après l'autre. Tes six réponses s'additionnent en un seul score sur %1\$s, classé parmi tous ceux qui jouent ce jour-là. Tu la trouveras en haut de l'écran %2\$s."
            AppLanguage.DE -> "Eine Frage aus jedem der sechs Spiele, nacheinander. Deine sechs Antworten ergeben zusammen eine einzige Punktzahl von höchstens %1\$s – im Ranking gegen alle, die an diesem Tag spielen. Du findest sie ganz oben auf dem Bildschirm „%2\$s“."
            AppLanguage.IT -> "Una domanda da ciascuno dei sei giochi, una dopo l'altra. Le tue sei risposte si sommano in un unico punteggio su %1\$s, in classifica con tutti quelli che giocano quel giorno. La trovi in cima alla schermata %2\$s."
            AppLanguage.HU -> "Egy kérdés mind a hat játékból, egymás után. A hat válaszod egyetlen, legfeljebb %1\$s pontos eredménnyé adódik össze, amelyet az aznap játszókéval vetnek össze. A(z) %2\$s képernyő tetején találod."
        }
        return formatTemplate(template, arg1, arg2)
    }

    @Composable
    fun tutorial_era_badge(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Also in %1\$s historical eras"
            AppLanguage.ES -> "También en %1\$s épocas históricas"
            AppLanguage.FR -> "Aussi dans %1\$s époques historiques"
            AppLanguage.DE -> "Auch in %1\$s historischen Epochen"
            AppLanguage.IT -> "Anche in %1\$s epoche storiche"
            AppLanguage.HU -> "Elérhető %1\$s történelmi korszakban is"
        }
        return formatTemplate(template, arg1)
    }

    val tutorial_eras_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Step into a historical era"
            AppLanguage.ES -> "Sumérgete en una época histórica"
            AppLanguage.FR -> "Plonge dans une époque historique"
            AppLanguage.DE -> "Tauche in eine historische Epoche ein"
            AppLanguage.IT -> "Immergiti in un'epoca storica"
            AppLanguage.HU -> "Lépj be egy történelmi korszakba"
        }

    /** [arg1] is the name of the Play tab, [arg2] the name of the All-time mode. */
    @Composable
    fun tutorial_eras_body(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "On the %1\$s screen, %2\$s is the daily set you know. Tap an era above your games to switch into that era's own game mode."
            AppLanguage.ES -> "En la pantalla %1\$s, «%2\$s» es el conjunto diario que ya conoces. Toca una época sobre tus juegos para pasar al modo de juego propio de esa época."
            AppLanguage.FR -> "Sur l'écran %1\$s, « %2\$s » est la série quotidienne que tu connais déjà. Touche une époque au-dessus de tes jeux pour passer dans le mode de jeu propre à cette époque."
            AppLanguage.DE -> "Auf dem Bildschirm „%1\$s“ ist „%2\$s“ das tägliche Set, das du schon kennst. Tippe über deinen Spielen auf eine Epoche, um in deren eigenen Spielmodus zu wechseln."
            AppLanguage.IT -> "Nella schermata %1\$s, «%2\$s» è il set giornaliero che già conosci. Tocca un'epoca sopra i tuoi giochi per passare alla modalità di gioco di quell'epoca."
            AppLanguage.HU -> "A(z) %1\$s képernyőn a(z) %2\$s az a napi csomag, amelyet már ismersz. Koppints egy korszakra a játékaid felett, hogy átválts az adott korszak saját játékmódjára."
        }
        return formatTemplate(template, arg1, arg2)
    }

    val tutorial_era_games_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Every era is a game of its own"
            AppLanguage.ES -> "Cada época es un juego propio"
            AppLanguage.FR -> "Chaque époque est un jeu à part entière"
            AppLanguage.DE -> "Jede Epoche ist ein eigenes Spiel"
            AppLanguage.IT -> "Ogni epoca è un gioco a sé"
            AppLanguage.HU -> "Minden korszak külön játék"
        }

    /** [arg1], [arg2] and [arg3] are the names of the three games that have eras. */
    @Composable
    fun tutorial_era_games_body(arg1: Any, arg2: Any, arg3: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s, %2\$s and %3\$s each get a fresh daily round with questions from that era only — plus their own streak, rank and stats. Friends and Profile can show any era's numbers too."
            AppLanguage.ES -> "%1\$s, %2\$s y %3\$s tienen cada uno una ronda diaria nueva con preguntas solo de esa época — además de su propia racha, clasificación y estadísticas. Amigos y Perfil también pueden mostrar los números de cualquier época."
            AppLanguage.FR -> "%1\$s, %2\$s et %3\$s ont chacun une nouvelle manche quotidienne avec des questions de cette époque uniquement — et leur propre série, classement et statistiques. Amis et Profil peuvent aussi afficher les chiffres de n'importe quelle époque."
            AppLanguage.DE -> "%1\$s, %2\$s und %3\$s bekommen jeweils eine frische Tagesrunde mit Fragen nur aus dieser Epoche — samt eigener Serie, eigenem Rang und eigenen Statistiken. Auch Freunde und Profil zeigen die Zahlen jeder Epoche."
            AppLanguage.IT -> "%1\$s, %2\$s e %3\$s hanno ciascuno un nuovo round giornaliero con domande solo di quell'epoca — più la propria serie, classifica e statistiche. Anche Amici e Profilo possono mostrare i numeri di qualsiasi epoca."
            AppLanguage.HU -> "A(z) %1\$s, %2\$s és %3\$s mindegyike friss napi kört kap csak az adott korszak kérdéseivel — plusz saját sorozattal, ranggal és statisztikával. A Barátok és a Profil bármelyik korszak adatait is meg tudja mutatni."
        }
        return formatTemplate(template, arg1, arg2, arg3)
    }

    val tutorial_closing_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You're All Set"
            AppLanguage.ES -> "Todo Listo"
            AppLanguage.FR -> "Tout Est Prêt"
            AppLanguage.DE -> "Bereit Zum Start"
            AppLanguage.IT -> "Tutto Pronto"
            AppLanguage.HU -> "Minden készen áll"
        }

    val tutorial_closing_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "A new round unlocks for each game every day — come back daily to keep your streak alive."
            AppLanguage.ES -> "Cada día se desbloquea una nueva ronda para cada juego — vuelve a diario para mantener viva tu racha."
            AppLanguage.FR -> "Une nouvelle manche se débloque chaque jour pour chaque jeu — reviens tous les jours pour garder ta série en vie."
            AppLanguage.DE -> "Jeden Tag wird für jedes Spiel eine neue Runde freigeschaltet — komm täglich zurück, um deine Serie am Leben zu erhalten."
            AppLanguage.IT -> "Ogni giorno si sblocca un nuovo round per ogni gioco — torna ogni giorno per mantenere viva la tua serie."
            AppLanguage.HU -> "Minden játékhoz naponta új kör nyílik meg — gyere vissza minden nap, hogy életben tartsd a sorozatodat."
        }
}
