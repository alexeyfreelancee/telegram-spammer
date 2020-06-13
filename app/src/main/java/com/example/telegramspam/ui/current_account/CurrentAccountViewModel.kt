package com.example.telegramspam.ui.current_account

import android.view.View
import androidx.lifecycle.*
import com.example.telegramspam.ACCOUNT
import com.example.telegramspam.NO_INTERNET
import com.example.telegramspam.SETTINGS
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.Event
import com.example.telegramspam.utils.connected
import kotlinx.coroutines.launch

class CurrentAccountViewModel(private val repository: Repository) : ViewModel() {
    private val _account = MutableLiveData<Account>()
    val account: LiveData<Account> get() = _account

    val openProxyDialog = MutableLiveData<Event<String>>()
    val openSettings = MutableLiveData<Event<String>>()
    val openChats = MutableLiveData<Event<Int>>()

    val startSpam = MutableLiveData<Event<HashMap<String, Any>>>()
    val toast = MutableLiveData<Event<String>>()
    private var accountId = 0

    fun updateProxy(
        proxyIp: String,
        proxyPort: Int,
        username: String,
        pass: String,
        proxyType: String
    ) {
        account.value?.let { acc ->
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

    fun openChats() {
       openChats.value = Event(accountId)
    }

    fun openSettings() {
        account.value?.let {
            openSettings.value =
                Event(it.databasePath)
        }
    }

    fun startSpam(view: View) {
        if (connected(view)) {
            viewModelScope.launch {
                account.value?.let {
                    val settings = repository.loadSettings(it.databasePath)
                    if (repository.checkSettings(settings, true)) {
                        val data = hashMapOf(
                            SETTINGS to settings!!,
                            ACCOUNT to it
                        )
                        startSpam.value = Event(data)
                    } else {
                        toast.value = Event("Заполните все поля в настройках")
                    }
                }
            }
        } else {
            toast.value = Event(NO_INTERNET)
        }

    }


    fun setupAccount(accountId: Int) = viewModelScope.launch {
        this@CurrentAccountViewModel.accountId = accountId
        _account.value = repository.loadAccount(accountId)
    }

    fun openProxyDialog() {
        openProxyDialog.value =
            Event("hello world")
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
