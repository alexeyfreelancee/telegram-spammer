package com.example.telegramspam.models

import org.drinkless.td.libcore.telegram.Client

sealed class ClientCreateResult {
    class Success(val client: Client) : ClientCreateResult()
    class Error(val message:String) : ClientCreateResult()
}