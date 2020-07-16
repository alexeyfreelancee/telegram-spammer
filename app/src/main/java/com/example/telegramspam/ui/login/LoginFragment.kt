package com.example.telegramspam.ui.login

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.telegramspam.R
import com.example.telegramspam.databinding.LoginFragmentBinding
import com.example.telegramspam.utils.toast
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class LoginFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var binding: LoginFragmentBinding
    private val factory by instance<LoginViewModelFactory>()

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)
        binding = LoginFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        viewModel.toast.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                toast(it.peekContent())
            }
        })
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)
        viewModel.openAccountsFragment.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                it.peekContent()
                navController.navigate(R.id.action_loginFragment_to_accountsFragment)
            }
        })
    }


}