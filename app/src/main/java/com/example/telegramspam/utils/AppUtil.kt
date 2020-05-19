package com.example.telegramspam.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList


fun log(vararg messages: Any?) {
    messages.forEach { msg ->
        if (msg != null) {
            Log.d("MEONER", msg.toString())
        }
    }
}

fun Fragment.toast(msg: String) {
    Snackbar.make(this.requireView(), msg, 1500).show()
}

fun List<String>.removeEmpty() : List<String>{
    val result= ArrayList<String>()
    this.forEach {
        if(it.isNotBlank()){
            result.add(it)
        }
    }
    return result
}
fun View.toast(msg: String) {
    Snackbar.make(this, msg, 1500).show()
}

fun connected(view: View) : Boolean{
    val context = view.context
    val cm =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
}
 fun List<String>.getRandom():String{
    val position =  Random().nextInt(this.size)
    return this[position]
}

fun generateRandomInt() : Int =  ThreadLocalRandom.current().nextInt(Int.MIN_VALUE, Int.MAX_VALUE - 1)

fun random(min:Int, max: Int) ={

}
fun Context.copyToClipboard(string: String) {
    val clipboard =
        this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("hello world", string))
}

fun View.gone(){
    this.visibility = View.GONE
}

fun View.visible(){
    this.visibility = View.VISIBLE
}


fun Activity.checkStoragePermission(): Boolean {
    if (
        ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1234
            )
        }
        return false
    } else {
        return true
    }
}