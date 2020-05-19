package com.example.telegramspam.ui.add_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.telegramspam.R
import com.example.telegramspam.databinding.AddAccountFragmentBinding
import com.example.telegramspam.data.telegram.AuthorizationListener
import com.example.telegramspam.utils.toast
import org.drinkless.td.libcore.telegram.TdApi
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class AddAccountFragment : Fragment(), KodeinAware,
    AuthorizationListener {
    override val kodein by kodein()
    private val factory by instance<AddAccountViewModelFactory>()
    private lateinit var binding: AddAccountFragmentBinding
    private lateinit var viewModel: AddAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(AddAccountViewModel::class.java)
        binding = AddAccountFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        viewModel.startAuth(this)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.closeClient()
    }

    override fun success(user: TdApi.User) {
        viewModel.saveUser(user)
        Navigation.findNavController(requireView())
            .navigate(R.id.action_addAccountFragment_to_accountsFragment)
        toast("Аккаунт добален")
    }

    override fun codeSend() {
        toast("Код отправлен")
    }

    override fun error(msg: String) {
        toast(msg)
    }
}
