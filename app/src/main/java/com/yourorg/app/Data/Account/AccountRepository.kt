package com.yourorg.app.Data.Account

import android.content.Context
import com.yourorg.app.Core.Notifications.ReminderScheduler
import com.yourorg.app.Data.Quiz.QuizProgressStore
import com.yourorg.app.Data.Quiz.ScoreHistoryStore
import com.yourorg.app.Data.Quiz.StatsStore
import com.yourorg.app.Data.Settings.SettingsStore
import com.yourorg.app.Data.Social.FriendsStore
import com.yourorg.app.Data.Social.generateUniqueFriendCode
import java.util.UUID

object AccountRepository {

    fun currentAccount(context: Context): UserAccount? {
        val id = SessionStore.currentAccountId(context) ?: return null
        return AccountStore.findById(context, id)
    }

    /** Creates the account on first sign-in with this provider identity, otherwise logs into the existing one. */
    suspend fun continueWithProvider(context: Context, provider: AuthProviderType): UserAccount {
        val result = mockSignIn(provider)
        val existing = AccountStore.findByProviderIdentity(context, provider.id, result.providerUserId)
        val account = existing ?: UserAccount(
            id = UUID.randomUUID().toString(),
            provider = provider.id,
            providerUserId = result.providerUserId,
            email = result.email,
            displayName = result.displayName,
            avatarUrl = result.avatarUrl,
            createdAt = System.currentTimeMillis(),
            friendCode = generateUniqueFriendCode(context)
        )
        if (existing == null) AccountStore.save(context, account)
        SessionStore.setCurrentAccountId(context, account.id)
        return account
    }

    fun updateDisplayName(context: Context, newName: String): UserAccount? {
        val current = currentAccount(context) ?: return null
        val updated = current.copy(displayName = newName)
        AccountStore.save(context, updated)
        return updated
    }

    fun signOut(context: Context) {
        SessionStore.setCurrentAccountId(context, null)
    }

    /** Deletes the account and everything tied to it — game progress cannot be recovered after this. */
    fun deleteAccount(context: Context) {
        val account = currentAccount(context) ?: return
        AccountStore.delete(context, account.id)
        SessionStore.setCurrentAccountId(context, null)
        QuizProgressStore.clearAll(context)
        ScoreHistoryStore.clearAll(context)
        StatsStore.clearAll(context)
        FriendsStore.clearAll(context)
        SettingsStore.setDailyReminderEnabled(context, false)
        ReminderScheduler.cancel(context)
    }
}
