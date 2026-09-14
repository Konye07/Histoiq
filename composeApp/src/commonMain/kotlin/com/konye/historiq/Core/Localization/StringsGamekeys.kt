package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable

object StringsGamekeys {
    val role_annalist: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Annalist"
            AppLanguage.ES -> "Analista"
            AppLanguage.FR -> "Annaliste"
            AppLanguage.DE -> "Annalist"
            AppLanguage.IT -> "Annalista"
        }

    val role_herald: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Herald"
            AppLanguage.ES -> "Heraldo"
            AppLanguage.FR -> "Héraut"
            AppLanguage.DE -> "Herold"
            AppLanguage.IT -> "Araldo"
        }

    val role_officer_of_arms: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Officer-of-Arms"
            AppLanguage.ES -> "Oficial de Armas"
            AppLanguage.FR -> "Officier d'Armes"
            AppLanguage.DE -> "Wappenoffizier"
            AppLanguage.IT -> "Ufficiale d'Armi"
        }

    val role_cosmographer: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Cosmographer"
            AppLanguage.ES -> "Cosmógrafo"
            AppLanguage.FR -> "Cosmographe"
            AppLanguage.DE -> "Kosmograph"
            AppLanguage.IT -> "Cosmografo"
        }

    val role_chronicler: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Chronicler"
            AppLanguage.ES -> "Cronista"
            AppLanguage.FR -> "Chroniqueur"
            AppLanguage.DE -> "Chronist"
            AppLanguage.IT -> "Cronista"
        }

    val role_chancellor: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Chancellor"
            AppLanguage.ES -> "Canciller"
            AppLanguage.FR -> "Chancelier"
            AppLanguage.DE -> "Kanzler"
            AppLanguage.IT -> "Cancelliere"
        }

    val title_year_guess: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Guess the Year"
            AppLanguage.ES -> "Adivina el Año"
            AppLanguage.FR -> "Devine l'Année"
            AppLanguage.DE -> "Rate das Jahr"
            AppLanguage.IT -> "Indovina l'Anno"
        }

    val title_casualties_multiplier: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Casualty Count"
            AppLanguage.ES -> "Cuenta de Bajas"
            AppLanguage.FR -> "Nombre de Pertes"
            AppLanguage.DE -> "Verlustzahl"
            AppLanguage.IT -> "Conteggio delle Perdite"
        }

    val title_flag_color_match: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Match the Flag Color"
            AppLanguage.ES -> "Iguala el Color de la Bandera"
            AppLanguage.FR -> "Assortis la Couleur du Drapeau"
            AppLanguage.DE -> "Finde die Flaggenfarbe"
            AppLanguage.IT -> "Abbina il Colore della Bandiera"
        }

    val title_territory_compare: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Bigger Territory"
            AppLanguage.ES -> "Mayor Territorio"
            AppLanguage.FR -> "Plus Grand Territoire"
            AppLanguage.DE -> "Größeres Gebiet"
            AppLanguage.IT -> "Territorio Maggiore"
        }

    val title_true_false: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "True or False"
            AppLanguage.ES -> "Verdadero o Falso"
            AppLanguage.FR -> "Vrai ou Faux"
            AppLanguage.DE -> "Wahr oder Falsch"
            AppLanguage.IT -> "Vero o Falso"
        }

    val title_blind_map_battle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Blind Map Battle"
            AppLanguage.ES -> "Batalla en Mapa Ciego"
            AppLanguage.FR -> "Bataille sur Carte Aveugle"
            AppLanguage.DE -> "Blindkarten-Schlacht"
            AppLanguage.IT -> "Battaglia sulla Mappa Cieca"
        }

}
