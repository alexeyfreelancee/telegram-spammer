package com.example.telegramspam

import android.app.Application
import androidx.room.Room
import com.example.telegramspam.data.Repository
import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.data.TelegramAccountsHelper
import com.example.telegramspam.ui.accounts.AccountsViewModelFactory
import com.example.telegramspam.ui.add_account.AddAccountViewModelFactory
import com.example.telegramspam.ui.current_account.CurrentAccountViewModelFactory
import com.example.telegramspam.ui.settings.SettingsViewModelFactory
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
        bind() from eagerSingleton { TelegramAccountsHelper(instance()) }
        bind() from eagerSingleton { Repository(instance(), instance()) }

        bind() from singleton { AccountsViewModelFactory(instance()) }
        bind() from singleton { AddAccountViewModelFactory(instance()) }
        bind() from singleton { CurrentAccountViewModelFactory(instance()) }
        bind() from singleton { SettingsViewModelFactory(instance()) }
    }
}