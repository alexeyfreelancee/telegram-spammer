package com.example.telegramspam.ui.current_account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.telegramspam.R

import com.example.telegramspam.databinding.CurrentAccountFragmentBinding
import com.example.telegramspam.ui.dialogs.ProxyDialog

import com.example.telegramspam.utils.ACC_ID
import com.example.telegramspam.utils.DB_PATH
import com.example.telegramspam.utils.log
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class CurrentAccountFragment : Fragment(), KodeinAware{
    override val kodein by kodein()
    private val factory by instance<CurrentAccountViewModelFactory>()
    private lateinit var binding : CurrentAccountFragmentBinding
    private lateinit var viewModel: CurrentAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(CurrentAccountViewModel::class.java)
        binding = CurrentAccountFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        setupViewModel()

        return binding.root
    }

    private fun setupViewModel(){
        arguments?.let { args->
            val accountId = args.getInt(ACC_ID, 0)
            log("accountId $accountId")
            viewModel.setupAccount(accountId)
        }
        viewModel.openProxyDialog.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                val dialog = ProxyDialog(requireContext(), viewModel)
                dialog.show()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)
        viewModel.openSettings.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                val bundle = Bundle()
                bundle.putString(DB_PATH, it.peekContent())
                navController.navigate(R.id.action_currentAccountFragment_to_settingsFragment, bundle)
            }

        })
    }



}
