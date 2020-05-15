package com.example.telegramspam.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.telegramspam.R
import com.example.telegramspam.data.TelegramAccountsHelper
import com.example.telegramspam.utils.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private val telegram by instance<TelegramAccountsHelper>()

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
