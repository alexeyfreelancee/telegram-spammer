package com.example.telegramspam.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.telegramspam.databinding.AccountRowBinding
import com.example.telegramspam.models.Account
import com.example.telegramspam.ui.accounts.AccountsViewModel
import com.example.telegramspam.utils.log

class AccountsListAdapter(private val viewModel: AccountsViewModel) :
    RecyclerView.Adapter<AccountsListAdapter.AccountViewHolder>() {
    private val items = ArrayList<Account>()

    fun submitList(newList: List<Account>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(account: Account) {
            val binding = DataBindingUtil.bind<AccountRowBinding>(itemView)?.apply {
                this.account = account
                this.viewmodel = viewModel
            }
            binding?.linear?.setOnLongClickListener {
                viewModel.openDeleteDialog(account.id)
                return@setOnLongClickListener true
            }
            binding?.linear?.setOnClickListener {
                viewModel.openAccount(account.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = AccountRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding.root)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(items[position])
    }
}