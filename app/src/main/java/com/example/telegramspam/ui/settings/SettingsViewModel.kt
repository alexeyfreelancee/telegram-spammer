package com.example.telegramspam.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.lifecycle.*
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Settings
import com.example.telegramspam.utils.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: Repository) : ViewModel(), UsersLoadingListener {
    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings> get() = _settings

    val dataLoading = MutableLiveData(false)
    val lastOnline = MutableLiveData("")
    private var dbPath = ""

    fun loadSettings(dbPath: String) = viewModelScope.launch {
        this@SettingsViewModel.dbPath = dbPath
        val settings = repository.loadSettings(dbPath) ?: Settings()
        settings.dbPath = dbPath
        _settings.value = settings
    }

    fun saveSettings() = viewModelScope.launch {
        settings.value?.let {
            it.maxOnlineDifference = calculateMaxOnlineDiff()
            log("settings saved", it)
            repository.saveSettings(it)
        }
    }

    fun loadList(view: View) {
        val settings = settings.value
        if (settings != null) {
            dataLoading.value = true
            repository.loadAccountList(settings, this, view)
        }

    }

    override fun loaded(users: String, view: View) {
        dataLoading.value = false
        val clipboard = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("laber", users))
        view.toast("Скопировано в буфер обмена")
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
            else -> 60 * 3
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
