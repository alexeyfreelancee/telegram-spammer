package com.example.telegramspam.ui.joiner

import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.telegramspam.NO_INTERNET
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.Event
import com.example.telegramspam.models.JoinerSettings
import com.example.telegramspam.utils.connected
import com.example.telegramspam.utils.log
import com.example.telegramspam.utils.toArrayList
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JoinerViewModel(private val repository: Repository) : ViewModel() {
    val accounts = MutableLiveData("")
    val groups = MutableLiveData("")
    val delay = MutableLiveData("")

    val dbAccounts = repository.loadAccountsAsync()
    val startJoiner = MutableLiveData<Event<JoinerSettings>>()
    val toast = MutableLiveData<Event<String>>()

    fun accountSelected(view: View, account:Account){
        val checkBox = view as CheckBox
        if(checkBox.isChecked){
            repository.accountSelectedJoiner(account)
            accounts.value = accounts.value!! + Gson().toJson(account) + ","
        }else{
            repository.accountDeselectedJoiner(account)
            accounts.value!!.replace(Gson().toJson(account), "")

        }
    }
    fun loadSettings() {
        viewModelScope.launch {
            val settings = repository.loadJoinerSettings()
            if(settings!=null){
                accounts.value = settings.accounts
                groups.value = settings.groups
                delay.value = settings.delay.toString()
            }

        }
    }




    fun startJoiner(view:View) {
        if(connected(view)){
            if(accounts.value!!.isNotBlank() && groups.value!!.isNotBlank() && delay.value!!.isNotBlank()){
                val joinerSettings = JoinerSettings(
                    groups = groups.value ?: "",
                    accounts = accounts.value ?: "",
                    delay = delay.value!!.toInt()
                )
                saveSettings()
                startJoiner.value = Event(joinerSettings)
            } else{
                toast.value = Event("Заполните все поля")
            }
        } else{
            toast.value = Event(NO_INTERNET)
        }


    }

    fun saveSettings() {
        repository.saveJoinerSettings(
            groups.value ?: "",
            accounts.value ?: "",
            delay.value ?: ""
        )
    }


}

@Suppress("UNCHECKED_CAST")
class JoinerViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return JoinerViewModel(
            repository
        ) as T
    }

}