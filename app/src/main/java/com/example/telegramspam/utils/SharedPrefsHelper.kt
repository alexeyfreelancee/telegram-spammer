package com.example.telegramspam.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsHelper(private val context:Context){
    private val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    fun savePassword(password:String){
        prefs.edit().putString("password", password).apply()
    }

    fun getPassword():String?{
        return prefs.getString("password", "")
    }
    fun checkRegistered():Boolean{
       return  prefs.getBoolean("registered", false)
    }

    fun setRegistered(){
        prefs.edit().putBoolean("registered", true).apply()
    }

    fun saveLogin(login:String){
        prefs.edit().putString("login",login).apply()
    }

    fun getLogin():String?{
       return prefs.getString("login", null)
    }
}