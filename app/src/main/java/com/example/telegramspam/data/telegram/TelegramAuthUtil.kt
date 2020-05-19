package com.example.telegramspam.data.telegram

import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class TelegramAuthUtil(
    private val telegram: TelegramClientUtil
) {
    private var client: Client? = null


    fun finishAuthentication(
        smsCode: String,

        listener: AuthorizationListener
    ) {
        client?.send(TdApi.CheckAuthenticationCode(smsCode)) {
            if (it is TdApi.Error) {
                listener.error(it.message)
            }
        }
    }

    fun enterPhoneNumber(phoneNumber: String, listener: AuthorizationListener) {
        client?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) {
            if (it is TdApi.Error) {
                listener.error(it.message)
            } else {
                log("sms code send")
                listener.codeSend()
            }
        }
    }

    fun closeClient(){
        client?.close()
    }
    fun startAuthentication(
        dbPath: String,
        listener: AuthorizationListener
    ) {
        log("auth started")
        client = Client.create({ state ->
            when (state) {
                is TdApi.Error -> {
                    listener.error(state.message)
                    log(state.message)
                }
                is TdApi.UpdateAuthorizationState -> {
                    when (state.authorizationState.constructor) {
                        TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                            log("success auth")
                            client?.send(TdApi.GetMe()) { user ->
                                client?.close()
                                listener.success(user as TdApi.User)
                            }

                        }
                        TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                            log("waiting params")
                            val params = telegram.generateParams(dbPath)
                            client?.send(TdApi.SetTdlibParameters(params)) {
                                if (it is TdApi.Error) {
                                    listener.error(it.message)
                                }
                            }
                        }
                        TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                            log("waiting encryption key")
                            client?.send(TdApi.CheckDatabaseEncryptionKey()) {
                                if (it is TdApi.Error) {
                                    listener.error(it.message)
                                }
                            }
                        }
                    }
                }
            }
        }, null, null)
    }

}

