package com.example.telegramspam.ui.inviter

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.telegramspam.NO_INTERNET
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Event
import com.example.telegramspam.models.InviterSettings
import com.example.telegramspam.utils.connected
import kotlinx.coroutines.launch

class InviterViewModel(private val repository: Repository) : ViewModel() {
    val accounts = MutableLiveData("")
    val chat = MutableLiveData("")
    val delay = MutableLiveData("")
    val inviteFrom = MutableLiveData("")

    val startInviter = MutableLiveData<Event<InviterSettings>>()
    val toast = MutableLiveData<Event<String>>()
    val showGuide = MutableLiveData<Event<String>>()

    fun loadSettings() {
        viewModelScope.launch {
            val settings = repository.loadInviterSettings()
            if(settings!=null){
                accounts.value = settings.accounts
                chat.value = settings.chat
                delay.value = settings.delay.toString()
                inviteFrom.value = settings.inviteFrom
            }

        }
    }

    fun showGuide(view:View){
        showGuide.value = Event("")
    }
    fun startInviter(view: View) {
        viewModelScope.launch {
            if (connected(view)) {
                if (accounts.value!!.isNotBlank() && delay.value!!.isNotBlank() && chat.value!!.isNotBlank()) {
                    if(repository.checkInviteFrom(inviteFrom.value!!)){
                        val settings = InviterSettings(
                            accounts = accounts.value!!,
                            chat = chat.value!!,
                            delay = delay.value!!.toInt(),
                            inviteFrom = inviteFrom.value!!
                        )
                        saveSettings()
                        startInviter.value = Event(settings)
                    } else{
                        toast.value = Event("Нет такого пользователя")
                    }

                } else {
                    toast.value = Event("Заполните все поля")
                }
            } else {
                toast.value = Event(NO_INTERNET)
            }

        }


    }


    fun saveSettings() {
        repository.saveInviterSettings(
            accounts.value!!,
            chat.value!!,
            delay.value!!,
            inviteFrom.value!!
        )
    }
}

@Suppress("UNCHECKED_CAST")
class InviterViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return InviterViewModel(
            repository
        ) as T
    }

}
