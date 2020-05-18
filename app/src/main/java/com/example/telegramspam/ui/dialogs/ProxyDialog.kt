package com.example.telegramspam.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.example.telegramspam.R
import com.example.telegramspam.ui.current_account.CurrentAccountViewModel
import com.example.telegramspam.HTTP
import com.example.telegramspam.SOCKS5

class ProxyDialog(
    context: Context,
    private val viewModel: CurrentAccountViewModel
) : Dialog(context) {

    private lateinit var proxyIp: EditText
    private lateinit var proxyPort: EditText
    private lateinit var username: EditText
    private lateinit var pass: EditText
    private lateinit var proxyType: Spinner

    private lateinit var ok: TextView
    private lateinit var cancel: TextView

    private val proxyTypes = arrayOf(
        SOCKS5,
        HTTP
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_proxy)


        setupSpinner()
        proxyIp = findViewById(R.id.proxyIp)
        proxyPort = findViewById(R.id.proxyPort)
        username = findViewById(R.id.username)
        pass = findViewById(R.id.pass)

        viewModel.account.value?.let { acc ->
            if(acc.proxyType == SOCKS5) proxyType.setSelection(0) else proxyType.setSelection(1)
            proxyIp.setText(acc.proxyIp)
            if (acc.proxyPort == 0) {
                proxyPort.setText("")
            } else {
                proxyPort.setText(acc.proxyPort.toString())
            }
            username.setText(acc.proxyUsername)
            pass.setText(acc.proxyPassword)
        }

        ok = findViewById(R.id.ok)
        cancel = findViewById(R.id.cancel)

        cancel.setOnClickListener { dismiss() }
        ok.setOnClickListener {
            val proxyPort =
                if (proxyPort.text.toString().isEmpty()) 0 else proxyPort.text.toString().toInt()
            val proxyType = proxyTypes[proxyType.selectedItemPosition]

            viewModel.updateProxy(
                proxyIp.text.toString(),
                proxyPort,
                username.text.toString(),
                pass.text.toString(),
                proxyType
            )

            dismiss()
        }
    }


    private fun setupSpinner() {
        proxyType = findViewById(R.id.proxyType)
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            proxyTypes
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        proxyType.adapter = adapter

    }

}