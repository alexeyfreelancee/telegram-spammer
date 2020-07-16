package com.example.telegramspam.ui.settings

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.*
import com.example.telegramspam.*
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Event
import com.example.telegramspam.models.AccountSettings
import com.example.telegramspam.utils.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: Repository) : ViewModel() {
    private val _settings = MutableLiveData<AccountSettings>()
    val accountSettings: LiveData<AccountSettings> get() = _settings

    val attachFile = MutableLiveData<Event<Any>>()
    val showGuide = MutableLiveData<Event<Any>>()
    val toast = MutableLiveData<Event<String>>()
    val loadUsers = MutableLiveData<Event<HashMap<String, Any>>>()

    val lastOnline = MutableLiveData("")
    val files = MutableLiveData<String>()

    private var dbPath = ""

    fun loadSettings(dbPath: String) = viewModelScope.launch {
        this@SettingsViewModel.dbPath = dbPath
        val settings = repository.loadSettings(dbPath) ?: AccountSettings()
        settings.dbPath = dbPath
        files.value = settings.files
        _settings.value = settings
    }

    fun saveSettings() = viewModelScope.launch {
        val settings = accountSettings.value
        val files = files.value
        if (settings != null) {
            settings.maxOnlineDifference = calculateMaxOnlineDiff()
            if (files != null) {
                settings.files = files
            }
            repository.saveSettings(settings)
        }

    }

    fun showGuide(view: View) {
        showGuide.value = Event(Any())
    }

    fun loadList(view: View) {
        if(connected(view)){
            viewModelScope.launch {
                if (repository.checkSettings(accountSettings.value, false)) {
                    val files = files.value
                    if (!files.isNullOrEmpty()) {
                        accountSettings.value?.files = files
                    }
                    accountSettings.value?.maxOnlineDifference = calculateMaxOnlineDiff()
                    loadUsers.value = Event(
                        hashMapOf(
                            SETTINGS to accountSettings.value!!,
                            ACCOUNT to repository.loadAccount(dbPath)
                        )
                    )
                } else {
                    toast.value =
                        Event("Введите чаты")
                }
            }
        }else{
            toast.value = Event(NO_INTERNET)
        }

    }

    fun attachFile(view: View) {
        attachFile.value = Event(Any())
    }

    fun     fileAttached(data: Intent, context: Context) {
        if (accountSettings.value != null) {
            val filesString =
                if (accountSettings.value!!.files.isEmpty()) repository.loadFilePaths(data, context)
                else "${accountSettings.value?.files},${repository.loadFilePaths(data, context)}"
            val resultList = ArrayList<String>()
            filesString.split(",").forEach {
                if (it.length > 3) {
                    resultList.add(it)
                }
            }
            if (resultList.size > 8) {
                toast.value =
                    Event("Максимум 8 файлов")
            } else {
                log(resultList)
                accountSettings.value?.files = filesString
                files.value = filesString
            }
        }
    }


    fun removeFile(position: Int) {
        val list = repository.removeFile(position, accountSettings.value)

        accountSettings.value?.files = list
        files.value = list
    }

    private fun calculateMaxOnlineDiff(): Long {
        return when (lastOnline.value) {
            HOUR -> 60 * 60
            HALF_HOUR -> (60 * 60) / 2
            THREE_HOURS -> 60 * 60 * 3
            TWELVE_HOUR -> 60 * 60 * 12
            DAY -> 60 * 60 * 24
            THREE_DAYS -> 60 * 60 * 24 * 3
            WEEK -> 60 * 60 * 24 * 7
            else -> 0
        }

    }
}

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            repository
        ) as T
    }

}
