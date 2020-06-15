package com.example.telegramspam.ui.current_chat

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.example.telegramspam.data.Repository
import com.example.telegramspam.utils.log
import kotlinx.coroutines.runBlocking
import org.drinkless.td.libcore.telegram.TdApi


class MessagesDataSource(
    private val repository: Repository,
    private val chatId: Long,
    private val accountId: Int
) : PageKeyedDataSource<Long, TdApi.Message>() {


    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, TdApi.Message>
    ) {
        runBlocking {
            val messages = repository.loadMessages(chatId, accountId, 0, params.requestedLoadSize /3)
            callback.onResult(messages, null, messages.last().id)
            log("load initial. size - ${params.requestedLoadSize}")
        }
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, TdApi.Message>) {
        runBlocking {
            val messages =
                repository.loadMessages(chatId, accountId, params.key, params.requestedLoadSize)
            callback.onResult(messages, messages.last().id)
            log("load after. size - ${params.requestedLoadSize}")
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, TdApi.Message>) {
        runBlocking {
            val messages = repository.loadMessages(chatId, accountId, 0, params.requestedLoadSize)
            val beforeMessages = repository.loadMessages(chatId, accountId, 0, params.requestedLoadSize, -params.requestedLoadSize)
            val id = beforeMessages.first().id
            callback.onResult(messages, id)
            log("load before. size - ${params.requestedLoadSize}")
        }
    }


}

class MessagesDataSourceFactory(
    private val repository: Repository,
    private val chatId: Long,
    private val accountId: Int
) : DataSource.Factory<Long, TdApi.Message>() {

    override fun create(): PageKeyedDataSource<Long, TdApi.Message> {
        log("created datasource")
        return MessagesDataSource(repository, chatId, accountId)
    }

}