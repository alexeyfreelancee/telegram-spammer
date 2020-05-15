package com.example.telegramspam.ui

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.example.telegramspam.R
import com.example.telegramspam.data.telegram.TelegramClientsUtil
import com.example.telegramspam.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.CheckDatabaseEncryptionKey
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val telegram by instance<TelegramClientsUtil>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkStoragePermission()) {
            telegram.init()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        telegram.init()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }





}
