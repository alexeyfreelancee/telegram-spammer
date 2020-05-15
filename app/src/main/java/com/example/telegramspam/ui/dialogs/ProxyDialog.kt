package com.example.telegramspam.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import com.example.telegramspam.R
import com.example.telegramspam.ui.current_account.CurrentAccountViewModel

class ProxyDialog(
    context: Context,
    private val viewModel: CurrentAccountViewModel
) : Dialog(context) {

    private lateinit var proxyIp: EditText
    private lateinit var proxyPort: EditText
    private lateinit var username: EditText
    private lateinit var pass: EditText

    private lateinit var ok: TextView
    private lateinit var cancel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_proxy)

        proxyIp = findViewById(R.id.proxyIp)
        proxyPort = findViewById(R.id.proxyPort)
        username = findViewById(R.id.username)
        pass = findViewById(R.id.pass)

        viewModel.account.value?.let { acc ->
            proxyIp.setText(acc.proxyIp)
            if (acc.proxyPort == 0) {
                proxyPort.setText("")
            } else {
                proxyPort.setText(acc.proxyPort)
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
            viewModel.updateProxy(
                proxyIp.text.toString(),
                proxyPort,
                username.text.toString(),
                pass.text.toString()
            )

            dismiss()
        }
    }
}