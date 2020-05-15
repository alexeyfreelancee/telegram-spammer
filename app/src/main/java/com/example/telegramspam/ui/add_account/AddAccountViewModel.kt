package com.example.telegramspam.ui.add_account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.telegramspam.data.Repository
import com.example.telegramspam.utils.ENTER_CODE
import com.example.telegramspam.utils.ENTER_PHONE
import com.example.telegramspam.utils.Event

class AddAccountViewModel(private val repository: Repository) : ViewModel() {
    val code = MutableLiveData("")
    val phone = MutableLiveData("")
    val error = MutableLiveData<Event<String>>()
    val proxyIp = MutableLiveData("")
    val proxyPort = MutableLiveData("")
    val success = MutableLiveData<Event<Boolean>>()

    fun confirm() {
        if (checkEmpty(true)) {
            if (proxyIp.value!!.isNotBlank() && proxyPort.value!!.isNotBlank()) {
                repository.addAccount(phone.value, code.value, proxyPort.value, proxyIp.value, success)
            } else {
                repository.addAccount(phone.value, code.value, success = success)
            }
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

    fun sendCode() {
        if (checkEmpty(false)) {
            repository.startAuth(phone.value)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class AddAccountViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddAccountViewModel(
            repository
        ) as T
    }

}
