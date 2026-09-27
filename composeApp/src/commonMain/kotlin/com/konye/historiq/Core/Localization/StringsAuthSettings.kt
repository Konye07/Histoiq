package com.konye.historiq.Core.Localization

import androidx.compose.runtime.Composable

object StringsAuthSettings {
    val auth_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Historiq"
            AppLanguage.ES -> "Historiq"
            AppLanguage.FR -> "Historiq"
            AppLanguage.DE -> "Historiq"
            AppLanguage.IT -> "Historiq"
        }

    val auth_subtitle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Sign in to track your daily scores"
            AppLanguage.ES -> "Inicia sesión para seguir tus puntuaciones diarias"
            AppLanguage.FR -> "Connecte-toi pour suivre tes scores quotidiens"
            AppLanguage.DE -> "Melde dich an, um deine täglichen Punktzahlen zu verfolgen"
            AppLanguage.IT -> "Accedi per tenere traccia dei tuoi punteggi giornalieri"
        }

    val auth_continue_with_google: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Continue with Google"
            AppLanguage.ES -> "Continuar con Google"
            AppLanguage.FR -> "Continuer avec Google"
            AppLanguage.DE -> "Weiter mit Google"
            AppLanguage.IT -> "Continua con Google"
        }

    val auth_continue_with_apple: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Continue with Apple"
            AppLanguage.ES -> "Continuar con Apple"
            AppLanguage.FR -> "Continuer avec Apple"
            AppLanguage.DE -> "Weiter mit Apple"
            AppLanguage.IT -> "Continua con Apple"
        }

    val auth_sign_in_failed: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Sign-in failed. Please try again."
            AppLanguage.ES -> "Error al iniciar sesión. Inténtalo de nuevo."
            AppLanguage.FR -> "Échec de la connexion. Veuillez réessayer."
            AppLanguage.DE -> "Anmeldung fehlgeschlagen. Bitte versuche es erneut."
            AppLanguage.IT -> "Accesso non riuscito. Riprova."
        }

    val auth_continue_without_login: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Continue without login"
            AppLanguage.ES -> "Continuar sin iniciar sesión"
            AppLanguage.FR -> "Continuer sans se connecter"
            AppLanguage.DE -> "Ohne Anmeldung fortfahren"
            AppLanguage.IT -> "Continua senza accedere"
        }

    val auth_guest_info: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Without an account, your scores and stats still count and the daily limit still applies — you just won't have a friend code to share, or be able to sync across devices."
            AppLanguage.ES -> "Sin una cuenta, tus puntuaciones y estadísticas igual cuentan y el límite diario sigue aplicándose; solo que no tendrás un código de amigo para compartir ni podrás sincronizar entre dispositivos."
            AppLanguage.FR -> "Sans compte, tes scores et statistiques comptent quand même et la limite quotidienne s'applique toujours — tu n'auras simplement pas de code ami à partager, ni la possibilité de synchroniser entre appareils."
            AppLanguage.DE -> "Auch ohne Konto zählen deine Punktzahlen und Statistiken, und das Tageslimit gilt weiterhin — du hast nur keinen Freundescode zum Teilen und kannst nicht geräteübergreifend synchronisieren."
            AppLanguage.IT -> "Senza un account, i tuoi punteggi e le tue statistiche contano comunque e il limite giornaliero si applica lo stesso — semplicemente non avrai un codice amico da condividere né potrai sincronizzare tra dispositivi."
        }

    val settings_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Settings"
            AppLanguage.ES -> "Ajustes"
            AppLanguage.FR -> "Paramètres"
            AppLanguage.DE -> "Einstellungen"
            AppLanguage.IT -> "Impostazioni"
        }

    val settings_display_name_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Display Name"
            AppLanguage.ES -> "Nombre para mostrar"
            AppLanguage.FR -> "Nom affiché"
            AppLanguage.DE -> "Anzeigename"
            AppLanguage.IT -> "Nome visualizzato"
        }

    val settings_display_name_subtitle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Shown on your profile and friend leaderboards."
            AppLanguage.ES -> "Se muestra en tu perfil y en las clasificaciones de amigos."
            AppLanguage.FR -> "Affiché sur ton profil et les classements entre amis."
            AppLanguage.DE -> "Wird in deinem Profil und den Freundeslisten angezeigt."
            AppLanguage.IT -> "Mostrato nel tuo profilo e nelle classifiche con gli amici."
        }

    val settings_save: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Save"
            AppLanguage.ES -> "Guardar"
            AppLanguage.FR -> "Enregistrer"
            AppLanguage.DE -> "Speichern"
            AppLanguage.IT -> "Salva"
        }

    @Composable
    fun settings_friend_code(arg1: Any): String {
        val template = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Your friend code: %1\$s"
            AppLanguage.ES -> "Tu código de amigo: %1\$s"
            AppLanguage.FR -> "Ton code ami : %1\$s"
            AppLanguage.DE -> "Dein Freundescode: %1\$s"
            AppLanguage.IT -> "Il tuo codice amico: %1\$s"
        }
        return formatTemplate(template, arg1)
    }

    val settings_languages_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Languages"
            AppLanguage.ES -> "Idiomas"
            AppLanguage.FR -> "Langues"
            AppLanguage.DE -> "Sprachen"
            AppLanguage.IT -> "Lingue"
        }

    val settings_coming_soon: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Coming soon"
            AppLanguage.ES -> "Próximamente"
            AppLanguage.FR -> "Bientôt disponible"
            AppLanguage.DE -> "Demnächst verfügbar"
            AppLanguage.IT -> "Prossimamente"
        }

    val settings_preferences_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Preferences"
            AppLanguage.ES -> "Preferencias"
            AppLanguage.FR -> "Préférences"
            AppLanguage.DE -> "Optionen"
            AppLanguage.IT -> "Preferenze"
        }

    val settings_haptics_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Haptics"
            AppLanguage.ES -> "Vibración"
            AppLanguage.FR -> "Vibrations"
            AppLanguage.DE -> "Vibration"
            AppLanguage.IT -> "Vibrazione"
        }

    val settings_haptics_subtitle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Buzz on right/wrong answers, perfect scores, and menu navigation."
            AppLanguage.ES -> "Vibra en respuestas correctas/incorrectas, puntuaciones perfectas y navegación por los menús."
            AppLanguage.FR -> "Vibre pour les bonnes/mauvaises réponses, les scores parfaits et la navigation dans les menus."
            AppLanguage.DE -> "Vibriert bei richtigen/falschen Antworten, perfekten Ergebnissen und der Menünavigation."
            AppLanguage.IT -> "Vibra per risposte giuste/sbagliate, punteggi perfetti e navigazione nei menu."
        }

    val settings_daily_reminder_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Daily Reminder"
            AppLanguage.ES -> "Recordatorio Diario"
            AppLanguage.FR -> "Rappel Quotidien"
            AppLanguage.DE -> "Tägliche Erinnerung"
            AppLanguage.IT -> "Promemoria Giornaliero"
        }

    val settings_daily_reminder_subtitle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "A once-a-day nudge to play today's quizzes."
            AppLanguage.ES -> "Un aviso diario para jugar los cuestionarios de hoy."
            AppLanguage.FR -> "Un rappel quotidien pour jouer aux quiz du jour."
            AppLanguage.DE -> "Eine tägliche Erinnerung, die heutigen Quiz zu spielen."
            AppLanguage.IT -> "Un promemoria giornaliero per giocare ai quiz di oggi."
        }

    val settings_dark_mode_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Dark Mode"
            AppLanguage.ES -> "Modo Oscuro"
            AppLanguage.FR -> "Mode Sombre"
            AppLanguage.DE -> "Dunkelmodus"
            AppLanguage.IT -> "Modalità Scura"
        }

    val settings_dark_mode_subtitle: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Follows your system setting until you switch it here."
            AppLanguage.ES -> "Sigue el ajuste de tu sistema hasta que lo cambies aquí."
            AppLanguage.FR -> "Suit le réglage de ton système jusqu'à ce que tu le changes ici."
            AppLanguage.DE -> "Folgt deiner Systemeinstellung, bis du sie hier änderst."
            AppLanguage.IT -> "Segue l'impostazione del sistema finché non la cambi qui."
        }

    val settings_account_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Account"
            AppLanguage.ES -> "Cuenta"
            AppLanguage.FR -> "Compte"
            AppLanguage.DE -> "Konto"
            AppLanguage.IT -> "Account"
        }

    val settings_email_label: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Email"
            AppLanguage.ES -> "Correo electrónico"
            AppLanguage.FR -> "E-mail"
            AppLanguage.DE -> "E-Mail"
            AppLanguage.IT -> "E-mail"
        }

    val settings_privacy_policy_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Privacy Policy"
            AppLanguage.ES -> "Política de Privacidad"
            AppLanguage.FR -> "Politique de Confidentialité"
            AppLanguage.DE -> "Datenschutzerklärung"
            AppLanguage.IT -> "Informativa sulla Privacy"
        }

    val settings_sign_out: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Sign Out"
            AppLanguage.ES -> "Cerrar Sesión"
            AppLanguage.FR -> "Se Déconnecter"
            AppLanguage.DE -> "Abmelden"
            AppLanguage.IT -> "Esci"
        }

    val settings_delete_account: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Delete Account"
            AppLanguage.ES -> "Eliminar Cuenta"
            AppLanguage.FR -> "Supprimer le Compte"
            AppLanguage.DE -> "Konto Löschen"
            AppLanguage.IT -> "Elimina Account"
        }

    val settings_guest_notice: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "You're playing as a guest — nothing is signed in to sign out of yet. Sign in to get a friend code, sync across devices, and back up your progress."
            AppLanguage.ES -> "Estás jugando como invitado: no hay ninguna sesión iniciada de la que cerrar sesión todavía. Inicia sesión para obtener un código de amigo, sincronizar entre dispositivos y respaldar tu progreso."
            AppLanguage.FR -> "Tu joues en tant qu'invité — il n'y a pas encore de session à laquelle te déconnecter. Connecte-toi pour obtenir un code ami, synchroniser entre appareils et sauvegarder ta progression."
            AppLanguage.DE -> "Du spielst als Gast — es gibt noch keine Sitzung, von der du dich abmelden könntest. Melde dich an, um einen Freundescode zu erhalten, geräteübergreifend zu synchronisieren und deinen Fortschritt zu sichern."
            AppLanguage.IT -> "Stai giocando come ospite — non c'è ancora nessuna sessione da cui uscire. Accedi per ottenere un codice amico, sincronizzare tra dispositivi ed eseguire il backup dei tuoi progressi."
        }

    val settings_sign_in: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Sign In"
            AppLanguage.ES -> "Iniciar Sesión"
            AppLanguage.FR -> "Se Connecter"
            AppLanguage.DE -> "Anmelden"
            AppLanguage.IT -> "Accedi"
        }

    val settings_delete_confirm_title: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Delete account?"
            AppLanguage.ES -> "¿Eliminar cuenta?"
            AppLanguage.FR -> "Supprimer le compte ?"
            AppLanguage.DE -> "Konto löschen?"
            AppLanguage.IT -> "Eliminare l'account?"
        }

    val settings_delete_confirm_body: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "This permanently deletes your account and ALL of your game data — every quiz's progress and score history. This cannot be undone and none of it can be recovered afterward."
            AppLanguage.ES -> "Esto elimina permanentemente tu cuenta y TODOS tus datos de juego: el progreso y el historial de puntuaciones de cada cuestionario. Esta acción no se puede deshacer y nada podrá recuperarse después."
            AppLanguage.FR -> "Cette action supprime définitivement ton compte et TOUTES tes données de jeu : la progression et l'historique des scores de chaque quiz. Cette action est irréversible et rien ne pourra être récupéré ensuite."
            AppLanguage.DE -> "Dies löscht dein Konto und ALLE deine Spieldaten dauerhaft — den Fortschritt und die Punkteverlauf-Historie jedes Quiz. Dies kann nicht rückgängig gemacht werden, und danach ist nichts mehr wiederherstellbar."
            AppLanguage.IT -> "Questa azione elimina definitivamente il tuo account e TUTTI i tuoi dati di gioco — i progressi e la cronologia dei punteggi di ogni quiz. Questa azione non può essere annullata e in seguito non sarà possibile recuperare nulla."
        }

    val settings_delete_confirm_button: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Delete"
            AppLanguage.ES -> "Eliminar"
            AppLanguage.FR -> "Supprimer"
            AppLanguage.DE -> "Löschen"
            AppLanguage.IT -> "Elimina"
        }

    val settings_cancel: String
        @Composable get() = when (LocalAppLanguage.current) {
            AppLanguage.EN -> "Cancel"
            AppLanguage.ES -> "Cancelar"
            AppLanguage.FR -> "Annuler"
            AppLanguage.DE -> "Abbrechen"
            AppLanguage.IT -> "Annulla"
        }

}
