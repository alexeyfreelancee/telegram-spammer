package com.example.telegramspam.utils

import org.drinkless.td.libcore.telegram.TdApi

interface AuthorizationListener {
    fun success(user: TdApi.User)
    fun codeSend()
    fun error(msg:String)
}