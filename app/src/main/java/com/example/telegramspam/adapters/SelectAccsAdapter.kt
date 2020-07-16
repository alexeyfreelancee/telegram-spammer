package com.example.telegramspam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramspam.databinding.SelectAccRowBinding
import com.example.telegramspam.models.Account
import com.example.telegramspam.ui.joiner.JoinerViewModel

class SelectAccsAdapter(private val viewModel:JoinerViewModel) : RecyclerView.Adapter<SelectAccsAdapter.SelectAccViewHolder>(){
    private val items = ArrayList<Account>()
     fun updateList(newList:List<Account>){
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class SelectAccViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        fun bind(account:Account){
                DataBindingUtil.bind<SelectAccRowBinding>(itemView)?.apply {
                    this.account = account
                    this.viewmodel = viewModel
                }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectAccViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SelectAccRowBinding.inflate(inflater, parent , false)
        return SelectAccViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SelectAccViewHolder, position: Int) {
        holder.bind(items[position])
    }
}