package com.example.telegramspam.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.telegramspam.data.telegram.TelegramClientUtil
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.ClientCreateResult
import com.example.telegramspam.models.InviterSettings
import com.example.telegramspam.utils.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InviterService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    private val clients = HashSet<String>()
    private val STOP_CURRENT = "com.example.telegramspam.services.STOP_CURRENT_INVITER"
    private val STOP_ALL = "com.example.telegramspam.services.STOP_ALL_INVITER"


    private fun generateStopAll(): Intent {
        val stopIntent = Intent(this@InviterService, InviterService::class.java)
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


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null && action.startsWith(STOP_CURRENT)) {
                log("stop current inviter")
                stop(action)
            } else if (action != null && action == STOP_ALL) {
                log("stop all inviter")
                stopAll()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val settings = Gson().fromJson(
                        intent.getStringExtra("settings"),
                        InviterSettings::class.java
                    )
                    startInviter(settings)
                }


            }

        }

        return START_NOT_STICKY
    }


    private suspend fun startInviter(settings: InviterSettings) {
        startForeground(
            INVITER_ID,
            createServiceNotification(
                "Inviter is running",
                generateStopAll()
            )
        )

        var errors = 0
        var success = 0

        val accounts = settings.accounts.toArrayList()
        val inviteFrom = Gson().fromJson(settings.inviteFrom, Account::class.java)

        when (val result = TelegramClientUtil.provideClient(inviteFrom)) {
            is ClientCreateResult.Success -> {
                val client = result.client
                clients.add(inviteFrom.phoneNumber)
                accounts.forEach { account ->
                    if (TelegramClientUtil.inviteUser(
                            client,
                            account,
                            settings.chat
                        )
                    ) success++ else errors++
                }
            }
            is ClientCreateResult.Error -> {
                errors++
            }
        }


        clients.forEach {
            TelegramClientUtil.stopClient(it)
        }
        sendNotificationInviter(success, errors)
        stopSelf()
    }
}