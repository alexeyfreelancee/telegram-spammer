package com.example.telegramspam.ui

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.telegramspam.R
import com.example.telegramspam.utils.*
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import java.lang.StringBuilder
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkStoragePermission()
        createNotificationChannels(applicationContext)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.info) {
            object : Dialog(this) {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContentView(R.layout.dialog_info)
                }
            }.show()
        }
        return true
    }

}
