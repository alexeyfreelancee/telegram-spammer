package com.example.telegramspam.ui.current_chat

import androidx.lifecycle.*
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Event
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class CurrentChatViewModel(private val repository: Repository) : ViewModel() {
    private val _messages = MutableLiveData<List<TdApi.Message>>()
    val messages: LiveData<List<TdApi.Message>> = _messages
    val openPhoto = MutableLiveData<Event<Int?>>()
    val message = MutableLiveData("")
    val dataLoading = MutableLiveData<Boolean>()
    val chat = MutableLiveData<TdApi.Chat>()
    val opponent = MutableLiveData<TdApi.User>()
    private val photos = ArrayList<String>()
    private var chatId: Long = 0
    var accountId: Int = 0


    private fun loadMessages(firstLoad: Boolean = false) {
        viewModelScope.launch {
            if(firstLoad) dataLoading.value = true
           _messages.value = repository.loadMessages(chatId,accountId)
            if(firstLoad) dataLoading.value = false
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            val success = repository.sendMessage(message.value!!, chatId.toInt(), accountId, photos)
            if (success) {
                message.value = ""
            }
        }

    }

    fun openPhoto(content:TdApi.MessageContent){
        when(content){
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
            loadMessages(true)
        }

    }

    fun startVoice(content: TdApi.MessageContent){
        if(content is TdApi.MessageVoiceNote){
            viewModelScope.launch {
                val voice = repository.getVoice(content)
                if(voice!=null) repository.playVoice(voice)
            }

        }
    }
    private val updatesHandler = Client.ResultHandler { update ->
        viewModelScope.launch {
            opponent.value = repository.getUser(chatId, accountId)
        }

        when (update.constructor) {
            TdApi.UpdateMessageEdited.CONSTRUCTOR -> loadMessages()
            TdApi.UpdateMessageSendSucceeded.CONSTRUCTOR -> loadMessages()
            TdApi.UpdateChatLastMessage.CONSTRUCTOR -> loadMessages()
            TdApi.UpdateNewMessage.CONSTRUCTOR -> loadMessages()
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