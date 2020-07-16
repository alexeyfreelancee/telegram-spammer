package com.example.telegramspam.ui.accounts

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.telegramspam.NO_INTERNET
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Event
import com.example.telegramspam.utils.connected
import kotlinx.coroutines.launch

class AccountsViewModel(private val repository: Repository) : ViewModel() {
    val accounts = repository.loadAccountsAsync()

    val openAccount = MutableLiveData<Event<Int>>()
    val addAccount = MutableLiveData<Event<String>>()
    val openDeleteDialog = MutableLiveData<Event<Int>>()
    val toast = MutableLiveData<Event<String>>()

    val startLoginActivity = MutableLiveData<Event<String>>()

    init {
        checkRegistered()
    }
    fun openDeleteDialog(id:Int){
        openDeleteDialog.value = Event(id)
    }

    private fun checkRegistered() {
        val registered = repository.checkRegistered()
        if(!registered){
            startLoginActivity.value = Event("")
        }
    }
    fun deleteAccount(id:Int) = viewModelScope.launch{
        repository.deleteAccount(id)
    }
    fun addAccount(view: View){
        if(connected(view)){
            addAccount.value = Event("hello world")
        }else{
            toast.value = Event(NO_INTERNET)
        }

    }

    fun openAccount(id:Int){
        openAccount.value = Event(id)
    }
}

@Suppress("UNCHECKED_CAST")
class AccountsViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AccountsViewModel(
            repository
        ) as T
    }

}
