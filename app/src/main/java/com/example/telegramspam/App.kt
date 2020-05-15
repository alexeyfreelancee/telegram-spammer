package com.example.telegramspam

import android.app.Application
import androidx.room.Room
import com.example.telegramspam.data.Repository
import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.data.telegram.TelegramClientsUtil
import com.example.telegramspam.ui.accounts.AccountsViewModel
import com.example.telegramspam.ui.accounts.AccountsViewModelFactory
import com.example.telegramspam.ui.add_account.AddAccountViewModelFactory
import org.drinkless.td.libcore.telegram.Client
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class App : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        bind() from eagerSingleton {
            Room.databaseBuilder(this@App, AppDatabase::class.java, "telegram_spam_db").build()
        }
        bind() from eagerSingleton { TelegramClientsUtil(instance()) }
        bind() from eagerSingleton { Repository(instance()) }

        bind() from singleton { AccountsViewModelFactory(instance()) }
        bind() from singleton { AddAccountViewModelFactory(instance(), instance()) }
    }
}