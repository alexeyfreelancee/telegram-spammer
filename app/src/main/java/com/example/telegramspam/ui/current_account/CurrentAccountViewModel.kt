package com.example.telegramspam.ui.current_account

import androidx.lifecycle.*
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Account
import com.example.telegramspam.utils.Event
import com.example.telegramspam.utils.log
import kotlinx.coroutines.launch

class CurrentAccountViewModel(private val repository: Repository) : ViewModel() {
    private val _account = MutableLiveData<Account>()
    val account: LiveData<Account> get() = _account

    val openProxyDialog = MutableLiveData<Event<String>>()
    val openSettings = MutableLiveData<Event<String>>()
    val startSpam = MutableLiveData<Event<String>>()

    private var accountId = 0

    fun updateProxy(
        proxyIp: String,
        proxyPort: Int,
        username: String,
        pass: String,
        proxyType: String
    ) {
        account.value?.let {acc->
            repository.updateProxy(
                proxyIp, proxyPort,
                username, pass, proxyType,
                acc.databasePath
            )
            _account.value = acc.apply {
                this.proxyType = proxyType
                this.proxyIp = proxyIp
                this.proxyUsername = username
                this.proxyPort = proxyPort
                this.proxyPassword = pass
            }
        }
    }

    fun openSettings(){
        account.value?.let {
            openSettings.value = Event(it.databasePath)
        }

    }

    fun startSpam(){
        account.value?.let {
            startSpam.value = Event(it.databasePath)
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
