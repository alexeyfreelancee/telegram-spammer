package com.example.telegramspam.ui.add_account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.telegramspam.ALREADY_EXISTS
import com.example.telegramspam.ENTER_CODE
import com.example.telegramspam.ENTER_PHONE
import com.example.telegramspam.WRONG_PHONE
import com.example.telegramspam.data.Repository
import com.example.telegramspam.data.telegram.AuthorizationListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.TdApi


class AddAccountViewModel(
    private val repository: Repository
) : ViewModel() {
    val code = MutableLiveData("")
    val phone = MutableLiveData("")
    val proxyIp = MutableLiveData<String>()
    val proxyPort = MutableLiveData<String>()
    val proxyUsername = MutableLiveData("")
    val proxyPassword = MutableLiveData("")
    val proxyType = MutableLiveData("")

    private val databasePath = repository.createDatabasePath()
    lateinit var listener: AuthorizationListener


    fun startAuth(listener: AuthorizationListener){
        this.listener = listener
        repository.startAuthentication(databasePath, listener)
    }
    fun confirm() = viewModelScope.launch {
        if (checkFields(true)) {
            repository.finishAuthentication(
                code.value!!,
                databasePath,
                listener
            )
        }
    }


    fun closeClient(){
        repository.closeClient()
    }

    fun sendCode() = viewModelScope.launch {
        if (checkFields(false)) {
            repository.enterPhoneNumber(
                phone.value!!,
                listener
            )
        }
    }

    private suspend fun checkFields(checkCode: Boolean): Boolean {
        return withContext(CoroutineScope(Dispatchers.Main).coroutineContext) {
            if (phone.value.isNullOrEmpty()) {
                listener.error(ENTER_PHONE)
                return@withContext false
            } else if (!phone.value!!.startsWith("+")) {
                listener.error(WRONG_PHONE)
                return@withContext false
            } else if (repository.checkNotExists(phone.value!!)) {
                listener.error(ALREADY_EXISTS)
                return@withContext false
            } else if (code.value.isNullOrEmpty() && checkCode) {
                listener.error(ENTER_CODE)
                return@withContext false
            }
            return@withContext true
        }
    }


     fun saveUser(user: TdApi.User) {
        repository.saveAccount(
            user,
            proxyIp.value,
            proxyPort.value?.toInt(),
            proxyUsername.value!!,
            proxyPassword.value!!,
            proxyType.value!!,
            databasePath
        )
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
