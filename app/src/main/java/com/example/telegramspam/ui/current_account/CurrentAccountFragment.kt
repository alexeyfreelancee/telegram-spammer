package com.example.telegramspam.ui.current_account

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.telegramspam.*

import com.example.telegramspam.databinding.CurrentAccountFragmentBinding
import com.example.telegramspam.ui.dialogs.ProxyDialog

import com.example.telegramspam.models.Account
import com.example.telegramspam.models.Settings
import com.example.telegramspam.services.SpammerService
import com.example.telegramspam.ui.MainActivity
import com.example.telegramspam.utils.log
import com.example.telegramspam.utils.toast
import com.google.gson.Gson
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
            viewModel.setupAccount(accountId)
        }

        viewModel.startSpam.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                startSpammerService(it.peekContent())
            }
        })
        viewModel.toast.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                toast(it.peekContent())
            }
        })
        viewModel.openProxyDialog.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                it.peekContent()
                val dialog = ProxyDialog(requireActivity(), viewModel)
                dialog.show()
            }
        })
    }

    private fun startSpammerService(data: HashMap<String, Any>){
        val intent = Intent(requireContext(), SpammerService::class.java)
        val settings = Gson().toJson(data[SETTINGS] as Settings)
        val account = Gson().toJson(data[ACCOUNT] as Account)
        intent.putExtra(SETTINGS, settings)
        intent.putExtra(ACCOUNT, account)

        requireActivity().startService(intent)
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

        viewModel.openChats.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                (requireActivity() as MainActivity).supportActionBar?.hide()
                navController.navigate(R.id.action_currentAccountFragment_to_chatFragment, bundleOf(
                    ACCOUNT_ID to it.peekContent()
                ))
            }
        })
    }


    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).supportActionBar?.show()
    }

}
