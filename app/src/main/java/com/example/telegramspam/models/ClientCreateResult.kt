package com.example.telegramspam.models

import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

sealed class ClientCreateResult {
    class Success(val client: Client) : ClientCreateResult()
    class Error(val message:String) : ClientCreateResult()
}

sealed class GetChatResult {
    class Success(val chat: TdApi.ChatTypeSupergroup) : GetChatResult()
    class Error(val message:String = "error") : GetChatResult()
}

sealed class GetUserResult {
    class Success(val user: TdApi.User) : GetUserResult()
    class Error(val message:String = "error") : GetUserResult()
}

sealed class GetChatMembersResult {
    class Success(val chatMembers: TdApi.ChatMembers) : GetChatMembersResult()
    class Error(val message:String = "error") : GetChatMembersResult()
}