package com.example.telegramspam.utils

import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.bumptech.glide.Glide
import com.example.telegramspam.R
import com.example.telegramspam.data.telegram.TelegramClientUtil
import com.example.telegramspam.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.TdApi
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("newMessages")
fun setNewMessages(view:TextView, account:Account?){
    if(account!=null){
        CoroutineScope(Dispatchers.IO).launch {
            val client = TelegramClientUtil.provideClient(account)
            if(client is ClientCreateResult.Success){
                val chats = TelegramClientUtil.loadChats(client.client)
                if(chats is GetChatsResult.Success){
                    var count = 0
                    val normalChats = ArrayList<TdApi.Chat>()
                    chats.chats.chatIds.forEach {
                        val normalChat = TelegramClientUtil.getChat(client.client, it)
                        if(normalChat is GetChat.Success){
                            normalChats.add(normalChat.chat)
                        }
                    }
                    normalChats.forEach {
                        count+=it.unreadCount
                    }
                    withContext(Dispatchers.Main){
                        if(count==0)view.gone()else view.visible()
                        view.text = "Непрочитанных сообщений: $count"
                    }
                }
            }


        }


    }
}
@BindingAdapter("unreadCount")
fun setUnreadCount(view: TextView, count: Int?){
    if(count!=null){
        view.text = "$count"
    }
}
@BindingAdapter("voice")
fun checkVoice(view: ImageView, content: TdApi.MessageContent?){
    if(content is TdApi.MessageVoiceNote){
        view.visible()
    }else{
        view.gone()
    }
}
@BindingAdapter("unread")
fun checkUnread(view: ImageView, views:Int?){
    if(views!=null){

        if(views < 2){
            view.setImageResource(R.drawable.ic_unread)
        }else{
            view.setImageResource(R.drawable.ic_read)
        }
    }
}
@BindingAdapter("photo")
fun setMsgPhoto(imageView: ImageView, content: TdApi.MessageContent?) {
    imageView.gone()
    if (content != null) {
        CoroutineScope(Dispatchers.IO).launch {

            when (content) {
                is TdApi.MessagePhoto -> {

                    val file = try {
                        TelegramClientUtil.downloadFile(content.photo.sizes[1].photo.id)
                    }catch (ex:Exception){
                        TelegramClientUtil.downloadFile(content.photo.sizes[0].photo.id)
                    }

                    if (file != null) {

                        withContext(Dispatchers.Main) {
                            imageView.visible()
                            Glide.with(imageView.context)
                                .load(file)
                                .into(imageView)
                        }

                    } else withContext(Dispatchers.Main) {

                        imageView.empty() }
                }
                is TdApi.MessageVoiceNote -> withContext(Dispatchers.Main) { imageView.visibility = View.INVISIBLE }
                is TdApi.MessageVideo -> {
                    val file = TelegramClientUtil.downloadFile(content.video.thumbnail?.photo?.id)
                    if (file != null) {
                        withContext(Dispatchers.Main) {
                            imageView.visible()
                            Glide.with(imageView.context)
                                .load(file)
                                .into(imageView)
                        }


                    } else withContext(Dispatchers.Main) { imageView.empty()}

                }
                else -> withContext(Dispatchers.Main) { imageView.gone() }
            }
        }

    }
}

@BindingAdapter("msgTime")
fun setMsgTime(textView: TextView, time: Int?) {
    if (time != null) {
        val date = Date(time.toLong() * 1000)
        textView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
}

@BindingAdapter("online")
fun checkOnline(textView: TextView, user: TdApi.User?) {
    if (user != null) {
        val status = user.status
        if (TelegramClientUtil.checkOnline(status, 60)) {
            textView.text = "Онлайн"
        } else {
            if (status is TdApi.UserStatusOffline) {
                val date = SimpleDateFormat(
                    "dd.MM HH:mm",
                    Locale.getDefault()
                ).format( Date(status.wasOnline.toLong() * 1000))
                textView.text = "Последний раз в сети $date"
            } else {
                textView.text = "Оффлайн"
            }

        }
    }
}

@BindingAdapter("message")
fun getMessageText(textView: TextView, content: TdApi.MessageContent?) {
    if (content != null) {
        textView.visible()
        val text = when (content) {
            is TdApi.SendMessageAlbum -> {
                val res = StringBuilder()
                content.inputMessageContents.forEach {
                    if (it is TdApi.InputMessageText) res.append(it.text.text)
                }
                res.toString()
            }
            is TdApi.MessageText -> content.text.text
            is TdApi.MessagePhoto -> {
                if(textView.id == R.id.last_msg){
                    "Photo"
                }else{
                    if(!content.caption.text.isNullOrEmpty()) content.caption.text
                    else{
                        textView.gone()
                        ""
                    }

                }

            }
            is TdApi.MessageVideo -> {
                if(textView.id == R.id.last_msg){
                    "Video"
                }else{
                    if(!content.caption.text.isNullOrEmpty()) content.caption.text
                    else{
                        textView.gone()
                        ""
                    }
                }
            }
            is TdApi.MessageDocument -> "Document"
            is TdApi.MessageAudio -> "Audio"
            is TdApi.MessageCall -> {
                if (content.duration == 0) "Canceled call"
                else {
                    val minutes = content.duration / 60
                    val seconds = content.duration % 60
                    val secString = if (seconds < 10) "0$seconds" else seconds.toString()
                    val minString = if (minutes < 10) "0$minutes" else minutes.toString()
                    "Call $minString:$secString"
                }
            }
            is TdApi.MessageChatAddMembers -> "Someone joined this channel"
            is TdApi.MessageChatJoinByLink -> "Someone joined this channel"
            is TdApi.MessageChatDeleteMember -> "Someone left this channel"
            is TdApi.MessageVoiceNote -> {
                val minutes = content.voiceNote.duration / 60
                val seconds = content.voiceNote.duration % 60
                val secString = if (seconds < 10) "0$seconds" else seconds.toString()
                val minString = if (minutes < 10) "0$minutes" else minutes.toString()
                "Voice note $minString:$secString"
            }
            is TdApi.MessageSticker -> "Sticker"
            else -> "..."
        }
        textView.text = text
    }

}

@BindingAdapter("phoneNumber")
fun setPhoneNumber(textView: TextView, phoneNumber: String?) {
    if (phoneNumber != null) {
        textView.text = "+$phoneNumber"
    }
}

@BindingAdapter("file")
fun setFile(view: ImageView, stringFiles: String?) {
    if (stringFiles != null) {

        val files = stringFiles.toArrayList()

        when (view.id) {
            R.id.first -> {
                if (files.isNotEmpty() && files[0].isNotEmpty()) {
                    view.loadFile(files[0])
                    view.visible()
                } else {
                    view.gone()
                }
            }
            R.id.second -> {
                if (files.size > 1) {
                    view.loadFile(files[1])
                    view.visible()
                } else {
                    view.gone()
                }
            }
            R.id.third -> {
                if (files.size > 2) {
                    view.loadFile(files[2])
                    view.visible()
                } else {
                    view.gone()
                }
            }
            R.id.fourth -> {
                if (files.size > 3) {
                    view.loadFile(files[3])
                    view.visible()
                } else {
                    view.gone()
                }
            }
            R.id.fifth -> {
                if (files.size > 4) {
                    view.loadFile(files[4])
                    view.visible()
                } else {
                    view.gone()
                }
            }
            R.id.sixth -> {
                if (files.size > 5) {
                    view.loadFile(files[5])
                    view.visible()
                } else {
                    view.gone()
                }
            }
            R.id.seventh -> {
                if (files.size > 6) {
                    view.loadFile(files[6])
                    view.visible()
                } else {
                    view.gone()
                }
            }
            R.id.eighth -> {
                if (files.size > 7) {
                    view.loadFile(files[7])
                    view.visible()
                } else {
                    view.gone()
                }
            }
        }
    } else {
        view.gone()
    }
}

fun ImageView.loadFile(path: String) {
    this.visible()
    Glide.with(this.context)
        .load(File(path))
        .into(this)
}


@BindingAdapter(value = ["selectedValue", "selectedValueAttrChanged"], requireAll = false)
fun bindSpinnerData(
    spinner: Spinner,
    newSelectedValue: String?,
    newTextAttrChanged: InverseBindingListener
) {
    spinner.onItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            newTextAttrChanged.onChange()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
    if (newSelectedValue != null) {
        val pos = (spinner.adapter as ArrayAdapter<String?>).getPosition(
            newSelectedValue
        )
        spinner.setSelection(pos, true)
    }
}

@InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
fun captureSelectedValue(spinner: Spinner): String? {
    return spinner.selectedItem as String
}

@BindingAdapter("proxy")
fun setProxy(textView: TextView, account: Account?) {
    if (account != null) {
        textView.text = "Proxy ${account.proxyIp}:${account.proxyPort} ${account.proxyType}"
    }
}