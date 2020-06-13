package com.example.telegramspam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramspam.databinding.ChatRowBinding
import com.example.telegramspam.models.Chat
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
            DataBindingUtil.bind<ChatRowBinding>(itemView)?.apply {
                viewmodel = viewModel
                this.chat = chat
            }
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