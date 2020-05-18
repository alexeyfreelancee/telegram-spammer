package com.example.telegramspam.data.telegram

import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class TelegramUtil(){
     suspend fun getChatMembers(
        client: Client,
        chat: TdApi.ChatTypeSupergroup,
        offset: Int
    ) : TdApi.ChatMembers{
        return suspendCancellableCoroutine {continuation ->
            client.send(TdApi.GetSupergroupMembers(chat.supergroupId, null, offset,200)){ members->
                if(members is TdApi.ChatMembers){
                    continuation.resume(members){}
                }

            }
        }
    }

     suspend fun getChat(client: Client, link: String) : TdApi.ChatTypeSupergroup{
        return suspendCancellableCoroutine {continuation->
            client.send(TdApi.SearchPublicChat(link)) { chat ->
                if (chat is TdApi.Chat && chat.type is TdApi.ChatTypeSupergroup) {
                    val type = chat.type
                    if (type is TdApi.ChatTypeSupergroup) {
                        continuation.resume(type){}
                    }
                }
            }
        }
    }

     suspend fun getUserFullInfo(client: Client, user: TdApi.User) : TdApi.UserFullInfo{
        return suspendCancellableCoroutine {continuation->
            client.send(TdApi.GetUserFullInfo(user.id)){ fullInfo->
                if(fullInfo is TdApi.UserFullInfo){
                    continuation.resume(fullInfo){}
                }
            }
        }
    }


     suspend fun getUser(client: Client, userId:Int) : TdApi.User{
        return suspendCancellableCoroutine {continuation->
            client.send(TdApi.GetUser(userId)){ user->
                if(user is TdApi.User){
                    continuation.resume(user){}
                }
            }
        }
    }
}