package com.example.telegramspam.ui.settings

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.telegramspam.ACCOUNT
import com.example.telegramspam.DB_PATH
import com.example.telegramspam.R
import com.example.telegramspam.SETTINGS
import com.example.telegramspam.databinding.SettingsFragmentBinding
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.Settings
import com.example.telegramspam.services.ParserService
import com.example.telegramspam.utils.*
import com.google.gson.Gson
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
        setupGuideDialog()
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)
        val accId = requireArguments().getString(DB_PATH) ?: ""
        viewModel.loadSettings(accId)
        viewModel.loadUsers.observe(viewLifecycleOwner, Observer {
            if (!it.hasBeenHandled) {
                startParserService(it.peekContent())
            }
        })
        viewModel.attachFile.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                it.peekContent()

                val intent = Intent().apply {
                    action = Intent.ACTION_GET_CONTENT
                    type = "image/* video/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                startActivityForResult(Intent.createChooser(intent, "Прикрепить файлы"), 1)
            }
        })
        viewModel.toast.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                toast(it.peekContent())
            }
        })

        viewModel.settings.observe(viewLifecycleOwner, Observer {
            when (it.maxOnlineDifference) {
                (60 * 60).toLong() -> binding.spinner.setSelection(1)
                ((60 * 60) / 2).toLong() -> binding.spinner.setSelection(2)
                (60 * 60 * 3).toLong() -> binding.spinner.setSelection(3)
                (60 * 60 * 12).toLong() -> binding.spinner.setSelection(4)
                (60 * 60 * 24).toLong() -> binding.spinner.setSelection(5)
                (60 * 60 * 24 * 3).toLong() -> binding.spinner.setSelection(6)
                (60 * 60 * 24 * 7).toLong() -> binding.spinner.setSelection(7)
                else -> binding.spinner.setSelection(0)
            }
        })
    }

    private fun startParserService(hashMap: HashMap<String, Any>){
        val settings = hashMap[SETTINGS] as Settings
        val account = hashMap[ACCOUNT] as Account
        val intent = Intent(requireContext(), ParserService::class.java).apply {
            putExtra(ACCOUNT, Gson().toJson(account))
            putExtra(SETTINGS, Gson().toJson(settings))
        }
        requireActivity().startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK && data!=null) {
            viewModel.fileAttached(data, requireContext())
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupGuideDialog() {
        viewModel.showGuide.observe(viewLifecycleOwner, Observer {
            if (!it.hasBeenHandled) {
                it.peekContent()
                object : Dialog(requireActivity()) {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContentView(R.layout.dialog_guide)
                    }
                }.show()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveSettings()
    }


}
