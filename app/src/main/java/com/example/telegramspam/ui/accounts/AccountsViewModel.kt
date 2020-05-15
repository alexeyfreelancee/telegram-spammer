package com.example.telegramspam.ui.accounts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.telegramspam.data.Repository
import com.example.telegramspam.utils.Event

class AccountsViewModel(private val repository: Repository) : ViewModel() {
    val accounts = repository.loadAccounts()

    val openAccount = MutableLiveData<Event<String>>()
    val addAccount = MutableLiveData<Event<String>>()

    fun addAccount(){
        addAccount.value = Event("hello world")
    }

    fun openAccount(id:String){
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
