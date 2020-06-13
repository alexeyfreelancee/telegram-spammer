package com.example.telegramspam.ui.chats

import androidx.lifecycle.*
import com.example.telegramspam.data.Repository
import com.example.telegramspam.models.Event

import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi

class ChatViewModel(private val repository: Repository) : ViewModel() {

    private val _chats = MutableLiveData<List<TdApi.Chat>>()
    val chats : LiveData<List<TdApi.Chat>> = _chats

    val openChat = MutableLiveData<Event<Long>>()
    val dataLoading = MutableLiveData<Boolean>()
    private var accountId = 0

    fun loadChats(){
        viewModelScope.launch {
            dataLoading.value = true
            _chats.value = repository.loadChats(accountId)
            dataLoading.value = false
        }
    }

    fun openChat(id:Long){
        openChat.value = Event(id)
    }
    fun setupAccount(accountId: Int)  {
        this.accountId = accountId
        loadChats()
    }
}

@Suppress("UNCHECKED_CAST")
class ChatsViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatViewModel(
            repository
        ) as T
    }

}