package com.example.telegramspam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramspam.databinding.ChatRowBinding
import com.example.telegramspam.ui.chats.ChatViewModel
import org.drinkless.td.libcore.telegram.TdApi

class ChatListAdapter(private val viewModel: ChatViewModel) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>(){
    private val items = ArrayList<TdApi.Chat>()

    fun fetchList(newList:List<TdApi.Chat>){
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(chat:TdApi.Chat){
            val binding =DataBindingUtil.bind<ChatRowBinding>(itemView)?.apply {
                viewmodel = viewModel
                this.chat = chat
            }

            val text = when(val msg = chat.lastMessage?.content){
                is TdApi.MessageText-> msg.text.text
                is TdApi.MessagePhoto -> "Photo"
                is TdApi.MessageVideo -> "Video"
                is TdApi.MessageDocument -> "Document"
                is TdApi.MessageAudio -> "Audio"
                is TdApi.MessageCall -> {
                    if(msg.duration == 0) "Canceled call"
                    else {
                        val minutes = msg.duration / 60
                        val seconds = msg.duration % 60
                        val secString = if(seconds < 10) "0$seconds" else seconds.toString()
                        val minString = if(minutes< 10) "0$minutes" else minutes.toString()
                        "Call $minString:$secString"
                    }
                }
                is TdApi.MessageVoiceNote -> {
                    val minutes = msg.voiceNote.duration  / 60
                    val seconds =msg.voiceNote.duration % 60
                    val secString = if(seconds < 10) "0$seconds" else seconds.toString()
                    val minString = if(minutes< 10) "0$minutes" else minutes.toString()
                    "Voice note $minString:$secString"
                }
                is TdApi.MessageSticker -> "Sticker"
                else->  "..."
            }

            binding?.lastMsg?.text= text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ChatRowBinding.inflate(inflater,parent,false)
        return ChatViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(items[position])
    }
}