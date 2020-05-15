package com.example.telegramspam.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.telegramspam.models.Account

@BindingAdapter("phoneNumber")
fun setPhoneNumber(textView: TextView, phoneNumber:String?){
    if(phoneNumber!=null){
        textView.text = "+$phoneNumber"
    }
}

@BindingAdapter("proxy")
fun setProxy(textView:TextView, account:Account?){
   if(account!=null){
       textView.text = "Proxy ${account.proxyIp}:${account.proxyPort}"
   }
}