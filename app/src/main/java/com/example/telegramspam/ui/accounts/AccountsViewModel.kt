package com.example.telegramspam.ui.accounts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Event
import kotlinx.coroutines.launch

class AccountsViewModel(private val repository: Repository) : ViewModel() {
    val accounts = repository.loadAccounts()

    val openAccount = MutableLiveData<Event<Int>>()
    val addAccount = MutableLiveData<Event<String>>()
    val openDeleteDialog = MutableLiveData<Event<Int>>()


    fun openDeleteDialog(id:Int){
        openDeleteDialog.value = Event(id)
    }

    fun deleteAccount(id:Int) = viewModelScope.launch{
        repository.deleteAccount(id)
    }
    fun addAccount(){
        addAccount.value = Event("hello world")
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
