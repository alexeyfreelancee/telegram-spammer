package com.example.telegramspam.data.telegram

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
        proxyIp: String? = null,
        proxyPort: Int? = null,
        proxyUsername: String = "",
        proxyPassword: String = "",
        proxyType: String = "",
        listener: AuthorizationListener
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val noProxy = proxyIp.isNullOrEmpty() || proxyPort == null || proxyPort == 0
            if (!noProxy) {
                log("added proxy")
                val proxyId = telegram.addProxy(
                    client,
                    proxyIp!!,
                    proxyPort!!,
                    proxyUsername,
                    proxyPassword,
                    proxyType
                )
                listener.proxyAdded(proxyId)
            } else{
                log("no proxy")
            }
            client?.send(TdApi.CheckAuthenticationCode(smsCode)) {
                if (it is TdApi.Error) {
                    listener.error(it.message)
                }
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

    fun closeClient() {
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
                            client?.send(TdApi.GetMe()) { user ->
                                if(user is TdApi.User){
                                    listener.success(user)
                                    client?.close()
                                }

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

