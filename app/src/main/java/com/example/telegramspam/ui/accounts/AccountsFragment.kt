package com.example.telegramspam.ui.accounts

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation

import com.example.telegramspam.R
import com.example.telegramspam.adapters.AccountsListAdapter
import com.example.telegramspam.databinding.AccountsFragmentBinding
import com.example.telegramspam.utils.ACCOUNT_ID
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class AccountsFragment : Fragment(), KodeinAware {
    private lateinit var binding: AccountsFragmentBinding
    private lateinit var viewModel: AccountsViewModel
    override val kodein by kodein()
    private val factory: AccountsViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(AccountsViewModel::class.java)
        binding = AccountsFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        setupAccounts()
        return binding.root
    }

    private fun setupAccounts(){
        val adapter = AccountsListAdapter(viewModel)
        binding.accountsList.adapter = adapter
        viewModel.accounts.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)
        viewModel.openAccount.observe(viewLifecycleOwner, Observer {
            navController.navigate(R.id.action_accountsFragment_to_currentAccountFragment, bundleOf(
                ACCOUNT_ID to it.peekContent()
            ))
        })
        viewModel.addAccount.observe(viewLifecycleOwner, Observer {
            navController.navigate(R.id.action_accountsFragment_to_addAccountFragment)
        })
    }

}
