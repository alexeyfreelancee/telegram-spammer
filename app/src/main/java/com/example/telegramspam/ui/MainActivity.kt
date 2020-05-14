package com.example.telegramspam.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.telegramspam.R
import com.example.telegramspam.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.CheckDatabaseEncryptionKey

class MainActivity : AppCompatActivity() {
    lateinit var client: Client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkStoragePermission()) {
            client = Client.create(ResultHandler(), null, null)
        }
        code.setOnClickListener {
            client.send(TdApi.CheckAuthenticationCode(code_text.text.toString()), null)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        client = Client.create(ResultHandler(), null, null)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    inner class ResultHandler : Client.ResultHandler {
        override fun onResult(state: TdApi.Object) {

            if (state is TdApi.UpdateAuthorizationState) {
                when (state.authorizationState.constructor) {
                    TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                        log("wait params")
                        val params = TdApi.TdlibParameters().apply {
                            databaseDirectory = dbDirectory()
                            useMessageDatabase = true
                            systemLanguageCode = "ru"
                            useSecretChats = false
                            apiId = API_ID
                            apiHash = API_HASH
                            deviceModel = "Desktop"
                            systemVersion = "Unknown"
                            applicationVersion = "1.0"
                            enableStorageOptimizer = true
                        }
                        client.send(TdApi.SetTdlibParameters(params)) {
                            log(it)
                        }
                    }
                    TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                        log("waiting code")
                    }
                    TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                        log("wait phone number")
                        client.send(TdApi.SetAuthenticationPhoneNumber("+79175878779", null)){
                            log(it)
                        }
                    }
                    TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                        log("wait encryption key")
                        client.send(CheckDatabaseEncryptionKey(), null)
                    }
                    TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                        log("success authorized")
                        client.send(TdApi.GetMe()) { result ->
                            if (result is TdApi.User) {
                                log(result)
                            } else {
                                log(result)
                            }
                        }
                    }


                }
            }

        }
    }


}
