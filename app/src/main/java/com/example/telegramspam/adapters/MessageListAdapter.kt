package com.example.telegramspam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.example.telegramspam.databinding.MyMessageRowBinding
import com.example.telegramspam.databinding.OpponentMessageRowBinding
import com.example.telegramspam.ui.current_chat.CurrentChatViewModel
import org.drinkless.td.libcore.telegram.TdApi

class MessageListAdapter(private val viewModel: CurrentChatViewModel) :
    PagedListAdapter<TdApi.Message, MessageListAdapter.MessageViewHolder>(MessageDiffUtil()) {
    private val MY_MSG = 1
    private val OPPONENT_MSG = 0





    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: TdApi.Message?) {

            if (itemViewType == MY_MSG) {
                DataBindingUtil.bind<MyMessageRowBinding>(itemView)?.apply {
                    viewmodel = viewModel
                    this.message = message
                }
            } else {
                DataBindingUtil.bind<OpponentMessageRowBinding>(itemView)?.apply {
                    viewmodel = viewModel
                    this.message = message
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        return if (msg?.senderUserId == viewModel.accountId) {
            MY_MSG
        } else {
            OPPONENT_MSG
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = if (viewType == MY_MSG) {
            MyMessageRowBinding.inflate(inflater, parent, false)
        } else {
            OpponentMessageRowBinding.inflate(inflater, parent, false)
        }
        return MessageViewHolder(binding.root)
    }



    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class MessageDiffUtil: DiffUtil.ItemCallback<TdApi.Message>() {


    override fun areItemsTheSame(old: TdApi.Message, new: TdApi.Message): Boolean {
        return old.id == new.id
    }

    override fun areContentsTheSame(old: TdApi.Message, new: TdApi.Message): Boolean {
        return old.content.constructor == new.content.constructor && old.date == new.date && old.chatId == new.chatId && old.editDate == new.editDate
    }

}