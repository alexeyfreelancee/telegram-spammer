package com.example.telegramspam.ui.inviter

import android.app.Dialog
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.telegramspam.R
import com.example.telegramspam.adapters.SelectAccsAdapter
import com.example.telegramspam.databinding.InviterFragmentBinding
import com.example.telegramspam.models.InviterSettings
import com.example.telegramspam.services.InviterService
import com.example.telegramspam.utils.toast
import com.google.gson.Gson
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class InviterFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory by instance<InviterViewModelFactory>()
    private lateinit var binding: InviterFragmentBinding
    private lateinit var viewModel: InviterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(InviterViewModel::class.java)
        binding = InviterFragmentBinding.inflate(inflater, container, false).apply {
            this.lifecycleOwner = viewLifecycleOwner
            this.viewmodel = viewModel
        }
        viewModel.showGuide.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                it.peekContent()
                showGuide()
            }
        })
        viewModel.startInviter.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                startInviter(it.peekContent())
            }
        })
        viewModel.toast.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                toast(it.peekContent())
            }
        })
        return binding.root
    }


    private fun showGuide(){
        object:Dialog(requireContext()){
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.inviter_guide)
            }
        }.show()
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadSettings()
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveSettings()
    }


    private fun startInviter(settings:InviterSettings){
        val intent = Intent(requireContext(), InviterService::class.java)
        intent.putExtra("settings", Gson().toJson(settings))
        requireActivity().startService(intent)
    }
}