package com.example.telegramspam.ui.add_account

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
import com.example.telegramspam.databinding.AddAccountFragmentBinding
import com.example.telegramspam.utils.toast
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class AddAccountFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory by instance<AddAccountViewModelFactory>()
    private lateinit var binding : AddAccountFragmentBinding
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

        viewModel.error.observe(viewLifecycleOwner, Observer {
            toast(it.peekContent())
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)
        viewModel.success.observe(viewLifecycleOwner, Observer {
            it.peekContent()
            navController.navigate(R.id.action_addAccountFragment_to_accountsFragment)
        })
    }
}
