package com.example.telegramspam.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.telegramspam.databinding.SettingsFragmentBinding
import com.example.telegramspam.utils.ACC_ID
import com.example.telegramspam.utils.DB_PATH
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class SettingsFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var binding: SettingsFragmentBinding
    private lateinit var viewModel: SettingsViewModel
    private val factory by instance<SettingsViewModelFactory>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupViewModel()
        binding = SettingsFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)
        val accId = requireArguments().getString(DB_PATH) ?: ""
        viewModel.loadSettings(accId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveSettings()
    }


}
