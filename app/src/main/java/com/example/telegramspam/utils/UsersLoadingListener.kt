package com.example.telegramspam.utils

import android.content.Context
import android.view.View

interface UsersLoadingListener {
    fun loaded(users:String)
}