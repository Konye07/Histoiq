package com.yourorg.app.Core.Localization

import androidx.compose.runtime.Composable

object StringsSocial {
    val friends_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Friends"
            AppLanguage.ES -> "Amigos"
            AppLanguage.FR -> "Amis"
            AppLanguage.DE -> "Freunde"
            AppLanguage.IT -> "Amici"
        }

    val friends_you_fallback: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You"
            AppLanguage.ES -> "Tú"
            AppLanguage.FR -> "Toi"
            AppLanguage.DE -> "Du"
            AppLanguage.IT -> "Tu"
        }

    val friends_filter_today: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Today"
            AppLanguage.ES -> "Hoy"
            AppLanguage.FR -> "Aujourd'hui"
            AppLanguage.DE -> "Heute"
            AppLanguage.IT -> "Oggi"
        }

    @Composable
    fun friends_filter_days_ago(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "-%1\$d d"
            AppLanguage.ES -> "-%1\$d d"
            AppLanguage.FR -> "-%1\$d j"
            AppLanguage.DE -> "-%1\$d T"
            AppLanguage.IT -> "-%1\$d g"
        }
        return formatTemplate(template, arg1)
    }

    @Composable
    fun friends_role_title_format(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s — %2\$s"
            AppLanguage.ES -> "%1\$s — %2\$s"
            AppLanguage.FR -> "%1\$s — %2\$s"
            AppLanguage.DE -> "%1\$s — %2\$s"
            AppLanguage.IT -> "%1\$s — %2\$s"
        }
        return formatTemplate(template, arg1, arg2)
    }

    @Composable
    fun friends_add_friend_pending(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Add Friend (%1\$d)"
            AppLanguage.ES -> "Añadir amigo (%1\$d)"
            AppLanguage.FR -> "Ajouter un ami (%1\$d)"
            AppLanguage.DE -> "Freund hinzufügen (%1\$d)"
            AppLanguage.IT -> "Aggiungi amico (%1\$d)"
        }
        return formatTemplate(template, arg1)
    }

    val friends_add_friend: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Add Friend"
            AppLanguage.ES -> "Añadir amigo"
            AppLanguage.FR -> "Ajouter un ami"
            AppLanguage.DE -> "Freund hinzufügen"
            AppLanguage.IT -> "Aggiungi amico"
        }

    @Composable
    fun friends_you_suffix(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s (You)"
            AppLanguage.ES -> "%1\$s (Tú)"
            AppLanguage.FR -> "%1\$s (Toi)"
            AppLanguage.DE -> "%1\$s (Du)"
            AppLanguage.IT -> "%1\$s (Tu)"
        }
        return formatTemplate(template, arg1)
    }

    val friends_not_played: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Not played"
            AppLanguage.ES -> "Sin jugar"
            AppLanguage.FR -> "Pas joué"
            AppLanguage.DE -> "Nicht gespielt"
            AppLanguage.IT -> "Non giocato"
        }

    val friends_feedback_added: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Friend added!"
            AppLanguage.ES -> "¡Amigo añadido!"
            AppLanguage.FR -> "Ami ajouté !"
            AppLanguage.DE -> "Freund hinzugefügt!"
            AppLanguage.IT -> "Amico aggiunto!"
        }

    val friends_feedback_already_connected: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You're already friends"
            AppLanguage.ES -> "Ya sois amigos"
            AppLanguage.FR -> "Vous êtes déjà amis"
            AppLanguage.DE -> "Ihr seid bereits befreundet"
            AppLanguage.IT -> "Siete già amici"
        }

    val friends_feedback_own_code: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "That's your own code"
            AppLanguage.ES -> "Ese es tu propio código"
            AppLanguage.FR -> "C'est votre propre code"
            AppLanguage.DE -> "Das ist dein eigener Code"
            AppLanguage.IT -> "Questo è il tuo codice"
        }

    val friends_feedback_not_found: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "No account found with that code"
            AppLanguage.ES -> "No se encontró ninguna cuenta con ese código"
            AppLanguage.FR -> "Aucun compte trouvé avec ce code"
            AppLanguage.DE -> "Kein Konto mit diesem Code gefunden"
            AppLanguage.IT -> "Nessun account trovato con questo codice"
        }

    val friends_add_friend_instructions: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Enter a friend's 9-digit code to connect."
            AppLanguage.ES -> "Introduce el código de 9 dígitos de un amigo para conectar."
            AppLanguage.FR -> "Entrez le code à 9 chiffres d'un ami pour vous connecter."
            AppLanguage.DE -> "Gib den 9-stelligen Code eines Freundes ein, um dich zu verbinden."
            AppLanguage.IT -> "Inserisci il codice a 9 cifre di un amico per connetterti."
        }

    val friends_code_placeholder: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "123456789"
            AppLanguage.ES -> "123456789"
            AppLanguage.FR -> "123456789"
            AppLanguage.DE -> "123456789"
            AppLanguage.IT -> "123456789"
        }

    val friends_sending: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Sending…"
            AppLanguage.ES -> "Enviando…"
            AppLanguage.FR -> "Envoi…"
            AppLanguage.DE -> "Wird gesendet…"
            AppLanguage.IT -> "Invio…"
        }

    val friends_send: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Send"
            AppLanguage.ES -> "Enviar"
            AppLanguage.FR -> "Envoyer"
            AppLanguage.DE -> "Senden"
            AppLanguage.IT -> "Invia"
        }

    val friends_incoming_requests: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Incoming Requests"
            AppLanguage.ES -> "Solicitudes recibidas"
            AppLanguage.FR -> "Demandes reçues"
            AppLanguage.DE -> "Eingehende Anfragen"
            AppLanguage.IT -> "Richieste ricevute"
        }

    val friends_accept: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Accept"
            AppLanguage.ES -> "Aceptar"
            AppLanguage.FR -> "Accepter"
            AppLanguage.DE -> "Annehmen"
            AppLanguage.IT -> "Accetta"
        }

    val friends_decline: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Decline"
            AppLanguage.ES -> "Rechazar"
            AppLanguage.FR -> "Refuser"
            AppLanguage.DE -> "Ablehnen"
            AppLanguage.IT -> "Rifiuta"
        }

    val friends_close: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Close"
            AppLanguage.ES -> "Cerrar"
            AppLanguage.FR -> "Fermer"
            AppLanguage.DE -> "Schließen"
            AppLanguage.IT -> "Chiudi"
        }

    val friends_guest_notice: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Sign in to add friends and see them on this leaderboard."
            AppLanguage.ES -> "Inicia sesión para agregar amigos y verlos en esta clasificación."
            AppLanguage.FR -> "Connecte-toi pour ajouter des amis et les voir dans ce classement."
            AppLanguage.DE -> "Melde dich an, um Freunde hinzuzufügen und sie in dieser Rangliste zu sehen."
            AppLanguage.IT -> "Accedi per aggiungere amici e vederli in questa classifica."
        }

    val profile_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Profile"
            AppLanguage.ES -> "Perfil"
            AppLanguage.FR -> "Profil"
            AppLanguage.DE -> "Profil"
            AppLanguage.IT -> "Profilo"
        }

    val profile_statistics: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Statistics"
            AppLanguage.ES -> "Estadísticas"
            AppLanguage.FR -> "Statistiques"
            AppLanguage.DE -> "Statistik"
            AppLanguage.IT -> "Statistiche"
        }

    @Composable
    fun profile_role_title_format(arg1: Any, arg2: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "%1\$s — %2\$s"
            AppLanguage.ES -> "%1\$s — %2\$s"
            AppLanguage.FR -> "%1\$s — %2\$s"
            AppLanguage.DE -> "%1\$s — %2\$s"
            AppLanguage.IT -> "%1\$s — %2\$s"
        }
        return formatTemplate(template, arg1, arg2)
    }

    val profile_not_played_yet: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Not played yet"
            AppLanguage.ES -> "Aún no jugado"
            AppLanguage.FR -> "Pas encore joué"
            AppLanguage.DE -> "Noch nicht gespielt"
            AppLanguage.IT -> "Non ancora giocato"
        }

    val profile_played: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Played"
            AppLanguage.ES -> "Jugadas"
            AppLanguage.FR -> "Parties"
            AppLanguage.DE -> "Gespielt"
            AppLanguage.IT -> "Giocate"
        }

    val profile_average: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Average"
            AppLanguage.ES -> "Promedio"
            AppLanguage.FR -> "Moyenne"
            AppLanguage.DE -> "Durchschnitt"
            AppLanguage.IT -> "Media"
        }

    val profile_perfect_runs: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Perfect Runs"
            AppLanguage.ES -> "Rondas Perfectas"
            AppLanguage.FR -> "Parties Parfaites"
            AppLanguage.DE -> "Perfekte Läufe"
            AppLanguage.IT -> "Partite Perfette"
        }

}
