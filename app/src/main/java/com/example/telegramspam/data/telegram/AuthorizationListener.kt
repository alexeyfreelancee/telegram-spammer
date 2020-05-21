package com.example.telegramspam.data.telegram

import org.drinkless.td.libcore.telegram.TdApi

interface AuthorizationListener {
    fun success(user: TdApi.User)
    fun codeSend()
    fun error(msg:String)
    fun proxyAdded(proxyId:Int?)
}