package com.example.telegramspam.utils

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.example.telegramspam.models.Account


@BindingAdapter("phoneNumber")
fun setPhoneNumber(textView: TextView, phoneNumber:String?){
    if(phoneNumber!=null){
        textView.text = "+$phoneNumber"
    }
}


@BindingAdapter(value = ["selectedValue", "selectedValueAttrChanged"], requireAll = false)
fun bindSpinnerData(
    spinner: Spinner,
    newSelectedValue: String?,
    newTextAttrChanged: InverseBindingListener
) {
    spinner.onItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            newTextAttrChanged.onChange()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
    if (newSelectedValue != null) {
        val pos = (spinner.adapter as ArrayAdapter<String?>).getPosition(
            newSelectedValue
        )
        spinner.setSelection(pos, true)
    }
}

@InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
fun captureSelectedValue(spinner: Spinner): String? {
    return spinner.selectedItem as String
}

@BindingAdapter("proxy")
fun setProxy(textView:TextView, account:Account?){
   if(account!=null){
       textView.text = "Proxy ${account.proxyIp}:${account.proxyPort} ${account.proxyType}"
   }
}