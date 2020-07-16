package com.example.telegramspam

import android.app.Application
import android.media.MediaPlayer
import androidx.room.Room
import com.example.telegramspam.data.Repository
import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.data.telegram.TelegramAuthUtil
import com.example.telegramspam.ui.accounts.AccountsViewModelFactory
import com.example.telegramspam.ui.add_account.AddAccountViewModelFactory
import com.example.telegramspam.ui.chats.ChatsViewModelFactory
import com.example.telegramspam.ui.current_account.CurrentAccountViewModelFactory
import com.example.telegramspam.ui.current_chat.CurrentChatViewModelFactory
import com.example.telegramspam.ui.inviter.InviterViewModelFactory
import com.example.telegramspam.ui.joiner.JoinerViewModelFactory
import com.example.telegramspam.ui.login.LoginViewModelFactory
import com.example.telegramspam.ui.settings.SettingsViewModelFactory
import com.example.telegramspam.utils.SharedPrefsHelper
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class App : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        bind() from eagerSingleton {
            Room.databaseBuilder(this@App, AppDatabase::class.java, "telegram_spam_db")
                .fallbackToDestructiveMigration()
                .build()
        }

        bind() from singleton { MediaPlayer() }
        bind() from eagerSingleton { TelegramAuthUtil() }
        bind() from singleton { SharedPrefsHelper(this@App) }
        bind() from eagerSingleton { Repository(instance(), instance(), instance(), instance()) }

        bind() from singleton { InviterViewModelFactory(instance()) }
        bind() from singleton { JoinerViewModelFactory(instance()) }
        bind() from singleton { ChatsViewModelFactory(instance())}
        bind() from singleton { LoginViewModelFactory(instance())}
        bind() from singleton { AccountsViewModelFactory(instance()) }
        bind() from singleton { AddAccountViewModelFactory(instance()) }
        bind() from singleton { CurrentAccountViewModelFactory(instance()) }
        bind() from singleton { SettingsViewModelFactory(instance()) }
        bind() from singleton { CurrentChatViewModelFactory(instance()) }
    }
}