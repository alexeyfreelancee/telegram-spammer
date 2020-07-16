package com.example.telegramspam.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.telegramspam.data.telegram.TelegramClientUtil
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.ClientCreateResult
import com.example.telegramspam.models.GetChatResult
import com.example.telegramspam.models.JoinerSettings
import com.example.telegramspam.utils.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JoinerService : Service() {
    private val clients = HashSet<String>()
    private val STOP_CURRENT = "com.example.telegramspam.services.STOP_CURRENT_JOINER"
    private val STOP_ALL = "com.example.telegramspam.services.STOP_ALL_JOINER"


    private fun generateStopAll(): Intent {
        val stopIntent = Intent(this@JoinerService, JoinerService::class.java)
        stopIntent.action = STOP_ALL
        return stopIntent
    }

    private fun stop(action: String) {
        val phone = action.split("@")[1]
        TelegramClientUtil.stopClient(phone)
    }

    private fun stopAll() {
        stopSelf()
        clients.forEach {
            TelegramClientUtil.stopClient(it)
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null && action.startsWith(STOP_CURRENT)) {
                log("stop current parsing")
                stop(action)
            } else if (action != null && action == STOP_ALL) {
                log("stop all parsing")
                stopAll()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val settings = Gson().fromJson(
                        intent.getStringExtra("settings"),
                        JoinerSettings::class.java
                    )
                    startJoiner(settings)
                }


            }

        }

        return START_NOT_STICKY
    }


    private suspend fun startJoiner(settings: JoinerSettings) {
        startForeground(
            JOINER_ID,
            createServiceNotification(
                "Joiner is running",
                generateStopAll()
            )
        )

        var errors = 0
        var success = 0

        val accounts = settings.accounts.toArrayList()
        val groups = settings.groups.toArrayList()

        accounts.forEach { json ->
            val account = Gson().fromJson(json, Account::class.java)
            when (val result = TelegramClientUtil.provideClient(account)) {
                is ClientCreateResult.Success -> {
                    val client = result.client
                    clients.add(account.phoneNumber)
                    groups.forEach { groupId ->
                        if (TelegramClientUtil.joinGroup(client, groupId)) success++ else errors++
                    }
                }
                is ClientCreateResult.Error -> {
                    errors++
                }
            }

        }
        clients.forEach {
            TelegramClientUtil.stopClient(it)
        }
        sendNotificationJoiner(success, errors)
        stopSelf()
    }
}