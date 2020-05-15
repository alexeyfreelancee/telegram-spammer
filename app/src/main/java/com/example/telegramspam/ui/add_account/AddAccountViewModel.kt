package com.example.telegramspam.ui.add_account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.telegramspam.data.Repository
import com.example.telegramspam.data.telegram.TelegramClientsUtil
import com.example.telegramspam.utils.ENTER_CODE
import com.example.telegramspam.utils.ENTER_PHONE
import com.example.telegramspam.utils.Event
import org.drinkless.td.libcore.telegram.TdApi

class AddAccountViewModel(
    private val repository: Repository,
    private val telegramClientsUtil: TelegramClientsUtil
) : ViewModel() {
    val code = MutableLiveData("")
    val phone = MutableLiveData("")
    val error = MutableLiveData<Event<String>>()
    val proxyIp = MutableLiveData<String>()
    val proxyPort = MutableLiveData<String>()
    val success = MutableLiveData<Event<TdApi.User>>()
    private val databasePath = repository.createDatabasePath()

    init {
        telegramClientsUtil.startAuthentication(databasePath, success, error)
    }

    fun confirm() {
        if (checkEmpty(true)) {
            telegramClientsUtil.confirm(code.value!!, databasePath)
        }
    }

    fun sendCode() {
        if (checkEmpty(false)) {
            telegramClientsUtil.sendSms(phone.value!!, databasePath)
        }
    }

    private fun checkEmpty(checkCode: Boolean): Boolean {
        if (code.value.isNullOrEmpty() && checkCode) {
            error.value = Event(ENTER_CODE)
            return false
        } else if (phone.value.isNullOrEmpty()) {
            error.value = Event(ENTER_PHONE)
            return false
        }
        return true
    }


    fun saveUser(user: TdApi.User) {
        repository.saveUser(
            user,
            proxyIp.value,
            proxyPort.value?.toInt(),
            databasePath
        )
    }

}

@Suppress("UNCHECKED_CAST")
class AddAccountViewModelFactory(
    private val repository: Repository,
    private val telegramClientsUtil: TelegramClientsUtil
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddAccountViewModel(
            repository, telegramClientsUtil
        ) as T
    }

}
