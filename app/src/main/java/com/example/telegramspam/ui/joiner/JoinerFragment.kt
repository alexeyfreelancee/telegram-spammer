package com.example.telegramspam.ui.joiner

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.telegramspam.R
import com.example.telegramspam.adapters.SelectAccsAdapter
import com.example.telegramspam.databinding.JoinerFragmentBinding
import com.example.telegramspam.models.JoinerSettings
import com.example.telegramspam.services.JoinerService
import com.example.telegramspam.services.PostWatchService
import com.example.telegramspam.utils.toast
import com.google.gson.Gson
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class JoinerFragment : Fragment(),KodeinAware {
    override val kodein by kodein()
    private val factory by instance<JoinerViewModelFactory>()
    private lateinit var binding:JoinerFragmentBinding

    private lateinit var viewModel: JoinerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        viewModel =ViewModelProvider(this,factory).get(JoinerViewModel::class.java)
        binding = JoinerFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        viewModel.startPostWatch.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                startPostWatch(it.peekContent())
            }
        })
        viewModel.startJoiner.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                startJoiner(it.peekContent())
            }
        })
        viewModel.toast.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                toast(it.peekContent())
            }
        })

        setupAccountsList()
        return binding.root
    }

    private fun setupAccountsList(){
        val adapter = SelectAccsAdapter(viewModel)
        binding.accountsList.adapter = adapter
        viewModel.dbAccounts.observe(viewLifecycleOwner, Observer {
            adapter.updateList(it)
        })

    }
    override fun onResume() {
        super.onResume()
        viewModel.loadSettings()
    }
    override fun onPause() {
        super.onPause()
        viewModel.saveSettings()
    }

    private fun startPostWatch(joinerSettings: JoinerSettings){
        val intent = Intent(requireContext(), PostWatchService::class.java)
        intent.putExtra("settings" ,Gson().toJson(joinerSettings))
        requireActivity().startService(intent)
    }

    private fun startJoiner(joinerSettings: JoinerSettings){
        val intent = Intent(requireContext(), JoinerService::class.java)
        intent.putExtra("settings" ,Gson().toJson(joinerSettings))
        requireActivity().startService(intent)
    }

}