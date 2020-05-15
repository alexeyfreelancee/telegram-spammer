package com.example.telegramspam.ui.current_account

import androidx.lifecycle.*
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Account
import com.example.telegramspam.utils.Event
import kotlinx.coroutines.launch

class CurrentAccountViewModel(private val repository: Repository) : ViewModel() {
    private val _account = MutableLiveData<Account>()
    val account: LiveData<Account> get() = _account

    val openProxyDialog = MutableLiveData<Event<String>>()

    private var accountId = 0

    fun updateProxy(proxyIp: String, proxyPort: Int, username: String, pass: String) {
        account.value?.let {
            repository.updateProxy(proxyIp, proxyPort, username, pass, it.databasePath)
        }

    }

    fun setupAccount(accountId: Int) = viewModelScope.launch {
        this@CurrentAccountViewModel.accountId = accountId
        _account.value = repository.loadAccount(accountId)
    }

    fun openProxyDialog() {
        openProxyDialog.value = Event("hello world")
    }

}

@Suppress("UNCHECKED_CAST")
class CurrentAccountViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CurrentAccountViewModel(
            repository
        ) as T
    }

}
