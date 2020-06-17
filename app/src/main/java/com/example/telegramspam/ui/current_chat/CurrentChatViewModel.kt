package com.example.telegramspam.ui.current_chat

import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Event
import com.example.telegramspam.utils.log
import com.example.telegramspam.utils.toArrayList
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class CurrentChatViewModel(private val repository: Repository) : ViewModel() {
    private val _messages = MutableLiveData<List<TdApi.Message>>()
    val scrollEvent = MutableLiveData<Event<Unit>>()
    val messages: LiveData<List<TdApi.Message>> = _messages
    val openPhoto = MutableLiveData<Event<Int?>>()
    val message = MutableLiveData("")
    val dataLoading = MutableLiveData<Boolean>()
    val chat = MutableLiveData<TdApi.Chat>()
    val opponent = MutableLiveData<TdApi.User>()
    val photos = MutableLiveData<String>()
    val attachFile = MutableLiveData<Event<Any>>()
    val toast = MutableLiveData<Event<String>>()
    private var chatId: Long = 0
    var accountId: Int = 0

    fun removeFile(position: Int) {
        val list = repository.removeFile(position, photos.value)
        photos.value = list
    }

    fun attachFile() {
        attachFile.value = Event(Any())
    }

    fun fileAttached(data: Intent, context: Context) {
        val filesString =
            if (photos.value.isNullOrEmpty()) repository.loadFilePaths(data, context)
            else "${photos.value},${repository.loadFilePaths(data, context)}"

        if (filesString.toArrayList().size > 4) {
            toast.value = Event("Максимум 4 фото")
        } else {
            photos.value = filesString
        }

    }

    private fun loadMessages(firstLoad: Boolean = false, scrollToBottom: Boolean = false) {
        viewModelScope.launch {
            if (firstLoad) dataLoading.value = true
            _messages.value = repository.loadMessages(chatId, accountId)
            if (firstLoad) dataLoading.value = false
            if (scrollToBottom) scrollEvent.value = Event(Unit)
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            val success = repository.sendMessage(
                message.value!!,
                chatId.toInt(),
                accountId,
                photos.value.toArrayList()
            )
            if (success) {
                message.value = ""
                photos.value = ""
            }
        }

    }

    fun openPhoto(content: TdApi.MessageContent) {
        when (content) {
            is TdApi.MessagePhoto -> openPhoto.value = Event(content.photo.sizes[1].photo.id)
            is TdApi.MessageVideo -> openPhoto.value = Event(content.video.thumbnail?.photo?.id)
        }
    }

    fun setupChat(chatId: Long, accountId: Int) {
        this.chatId = chatId
        this.accountId = accountId
        viewModelScope.launch {
            chat.value = repository.getChat(chatId, accountId)
            opponent.value = repository.getUser(chatId, accountId)
            val client = repository.provideClient(accountId)
            client?.setUpdatesHandler(updatesHandler)
            loadMessages(firstLoad = true, scrollToBottom = true)
        }

    }

    fun startVoice(content: TdApi.MessageContent) {
        if (content is TdApi.MessageVoiceNote) {
            viewModelScope.launch {
                val voice = repository.getVoice(content)
                if (voice != null) repository.playVoice(voice)
            }

        }
    }

    private val updatesHandler = Client.ResultHandler { update ->
        viewModelScope.launch {
            opponent.value = repository.getUser(chatId, accountId)
        }

        when (update) {
            is TdApi.UpdateDeleteMessages -> {
                if (update.isPermanent){
                    loadMessages()

                }
            }
            is TdApi.UpdateMessageEdited -> {

                loadMessages()
            }
            is TdApi.UpdateNewMessage -> {

                loadMessages()
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class CurrentChatViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CurrentChatViewModel(
            repository
        ) as T
    }

}