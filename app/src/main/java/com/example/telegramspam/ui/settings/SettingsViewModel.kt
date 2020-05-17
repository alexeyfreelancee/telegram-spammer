package com.example.telegramspam.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.lifecycle.*
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Settings
import com.example.telegramspam.utils.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: Repository) : ViewModel(), UsersLoadingListener {
    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings> get() = _settings

    val attachFile = MutableLiveData<Event<Any>>()
    val showGuide = MutableLiveData<Event<Any>>()
    val dataLoading = MutableLiveData(false)
    val lastOnline = MutableLiveData("")
    val toast = MutableLiveData<Event<String>>()
    val usersLoaded = MutableLiveData<Event<String>>()
    val files = MutableLiveData<String>()

    private var dbPath = ""
    private var loaded = false
    fun loadSettings(dbPath: String) = viewModelScope.launch {

        this@SettingsViewModel.dbPath = dbPath
        val settings = repository.loadSettings(dbPath) ?: Settings()
        log(settings)
        settings.dbPath = dbPath
        files.value = settings.files
        _settings.value = settings
    }

    fun saveSettings() = viewModelScope.launch {
        settings.value?.let {
            repository.saveSettings(it.apply { maxOnlineDifference = calculateMaxOnlineDiff() })
        }
    }

    fun showGuide(view: View) {
        showGuide.value = Event(Any())
    }

    fun loadList(view: View) {
        if (repository.checkSettings(settings.value, false)) {
            dataLoading.value = true
            loaded = false
            Handler().postDelayed({
                if (!loaded) {
                    dataLoading.value = false
                    toast.value = Event("Ошибка :(")
                }
            }, 20000)
            repository.loadAccountList(settings.value!!, this)
        } else {
            toast.value = Event("Введите чаты")
        }
    }

    override fun loaded(users: String, success: Boolean) {
        viewModelScope.launch {
            loaded = true
            dataLoading.value = false
            if (success) {
                toast.value = Event("Ошибка :(")
            } else {
                usersLoaded.value = Event(users)
            }
        }
    }

    fun attachFile(view: View) {
        attachFile.value = Event(Any())
    }

    fun fileAttached(data: Intent, context: Context) {
        if (settings.value != null) {
            val list =
                if (settings.value!!.files.isEmpty()) repository.loadFilePaths(data, context)
                else "${settings.value?.files},${repository.loadFilePaths(data, context)}"
            val resultList = ArrayList<String>()
            list.split(",").forEach {
                if (it.length > 3) {
                    resultList.add(it)
                }
            }
            if (resultList.size > 8) {
                toast.value = Event("Максимум 8 файлов")
            } else {
                log(resultList)
                settings.value?.files = list
                files.value = list
            }
        }
    }

    fun copyToClipboard(users: String, context: Context) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("users list", users))
    }

    fun removeFile(position: Int) {
        val list = repository.removeFile(position, settings.value)

        settings.value?.files = list
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
