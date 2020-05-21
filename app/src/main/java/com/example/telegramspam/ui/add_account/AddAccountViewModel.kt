package com.example.telegramspam.ui.add_account

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.telegramspam.*
import com.example.telegramspam.data.Repository
import com.example.telegramspam.data.telegram.AuthorizationListener
import com.example.telegramspam.models.Event
import com.example.telegramspam.utils.connected
import com.example.telegramspam.utils.log
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
    val toast = MutableLiveData<Event<String>>()
    var proxyId = 0
    private val databasePath = repository.createDatabasePath()
    lateinit var listener: AuthorizationListener


    fun startAuth(listener: AuthorizationListener){
        this.listener = listener
        repository.startAuthentication(databasePath, listener)
    }
    fun confirm(view: View)  {
        viewModelScope.launch {
            if(connected(view)){
                if (checkFields(true)) {
                    repository.finishAuthentication(
                        code.value!!,
                        proxyIp.value,
                        proxyPort.value?.toInt(),
                        proxyUsername.value!!,
                        proxyPassword.value!!
                        ,proxyType.value!!,

                        listener)
                }
            } else{
                toast.value = Event(NO_INTERNET)
            }


        }
    }

    fun proxyAdded(proxyId:Int?){
        log("listener proxy added $proxyId")
        if(proxyId!=null){

            this.proxyId = proxyId
        }
    }




    fun sendCode(view: View) {
        viewModelScope.launch {
            if(connected(view)){
                if (checkFields(false)) {
                    repository.enterPhoneNumber(
                        phone.value!!,
                        listener
                    )
                }
            } else{
                toast.value = Event(NO_INTERNET)
            }

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
            proxyId,
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
