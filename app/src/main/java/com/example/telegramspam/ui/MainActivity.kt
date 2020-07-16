package com.example.telegramspam.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import com.example.telegramspam.R
import com.example.telegramspam.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import java.lang.StringBuilder
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Client.execute(TdApi.SetLogVerbosityLevel(0))
        checkStoragePermission()
        checkLicense()
        createNotificationChannels(applicationContext)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun checkLicense(){
        if(connected()){
            FirebaseDatabase.getInstance().getReference("enabled").addValueEventListener(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    val enabled = snapshot.value as Boolean
                    if(!enabled) {
                        finish()
                        Toast.makeText(applicationContext, "App is disabled", Toast.LENGTH_SHORT).show()
                    }
                }

            })
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        when(item.itemId){
            R.id.info ->{
                object : Dialog(this) {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContentView(R.layout.dialog_info)
                    }
                }.show()
            }
            R.id.inviter ->{
                navController.navigate(R.id.inviterFragment)
            }
            R.id.joiner ->{
                navController.navigate(R.id.joinerFragment)
            }
        }

        return true
    }

    private fun checkStoragePermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    1234
                )
            }
            return false
        } else {
            return true
        }
    }
}
