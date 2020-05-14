package com.example.telegramspam

import android.app.Application
import androidx.room.Room
import com.example.telegramspam.data.Repository
import com.example.telegramspam.data.database.AppDatabase
import org.drinkless.td.libcore.telegram.Client
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance

class App : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        bind() from eagerSingleton {
            Room.databaseBuilder(this@App, AppDatabase::class.java, "telegram_spam_db").build()
        }

        bind() from eagerSingleton { Repository(instance()) }
    }
}