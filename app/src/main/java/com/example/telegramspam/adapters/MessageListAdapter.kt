package com.example.telegramspam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

import com.example.telegramspam.databinding.MyMessageRowBinding
import com.example.telegramspam.databinding.OpponentMessageRowBinding
import com.example.telegramspam.ui.current_chat.CurrentChatViewModel
import org.drinkless.td.libcore.telegram.TdApi

class MessageListAdapter(private val viewModel: CurrentChatViewModel) :
    RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>() {
    private val MY_MSG = 1
    private val OPPONENT_MSG = 0

    private val items = ArrayList<TdApi.Message>()

    fun fetchList(newList: List<TdApi.Message>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: TdApi.Message) {

            if(itemViewType == MY_MSG){
                DataBindingUtil.bind<MyMessageRowBinding>(itemView)?.apply {
                    viewmodel = viewModel
                    this.message = message
                }
            } else{
               DataBindingUtil.bind<OpponentMessageRowBinding>(itemView)?.apply {
                    viewmodel = viewModel
                    this.message = message
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val msg = items[position]
        return if(msg.senderUserId == viewModel.accountId){
            MY_MSG
        } else {
            OPPONENT_MSG
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = if(viewType == MY_MSG){
            MyMessageRowBinding.inflate(inflater, parent, false)
        }else {
            OpponentMessageRowBinding.inflate(inflater,parent,false)
        }
        return MessageViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(items[position])
    }
}