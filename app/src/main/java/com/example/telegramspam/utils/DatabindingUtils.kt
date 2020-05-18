package com.example.telegramspam.utils

import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.bumptech.glide.Glide
import com.example.telegramspam.R
import com.example.telegramspam.models.Account
import java.io.File


@BindingAdapter("phoneNumber")
fun setPhoneNumber(textView: TextView, phoneNumber:String?){
    if(phoneNumber!=null){
        textView.text = "+$phoneNumber"
    }
}

@BindingAdapter("file")
fun setFile(view: ImageView, stringFiles:String?){
    if(stringFiles!=null){
        val files = ArrayList<String>()
        stringFiles.split(",").forEach {
            if(it.length > 3){
                files.add(it)
            }
        }
        when(view.id){
            R.id.first ->{
                if(files.isNotEmpty() && files[0].isNotEmpty()){
                    view.loadFile(files[0])
                }else{
                    view.gone()
                }
            }
            R.id.second ->{
                if(files.size > 1){
                    view.loadFile(files[1])
                } else{
                    view.gone()
                }
            }
            R.id.third ->{
                if(files.size > 2){
                    view.loadFile(files[2])
                } else{
                    view.gone()
                }
            }
            R.id.fourth ->{
                if(files.size > 3){
                    view.loadFile(files[3])
                } else{
                    view.gone()
                }
            }
            R.id.fifth ->{
                if(files.size > 4){
                    view.loadFile(files[4])
                } else{
                    view.gone()
                }
            }
            R.id.sixth ->{
                if(files.size > 5){
                    view.loadFile(files[5])
                } else{
                    view.gone()
                }
            }
            R.id.seventh ->{
                if(files.size > 6){
                    view.loadFile(files[6])
                } else{
                    view.gone()
                }
            }
            R.id.eighth ->{
                if(files.size > 7){
                    view.loadFile(files[7])
                } else{
                    view.gone()
                }
            }
        }
    }else{
        view.gone()
    }
}
fun ImageView.loadFile(path:String){
    this.visible()
    Glide.with(this.context)
        .load(File(path))
        .into(this)
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