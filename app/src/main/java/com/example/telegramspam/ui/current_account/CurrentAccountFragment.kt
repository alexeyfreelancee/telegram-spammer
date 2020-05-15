package com.example.telegramspam.ui.current_account

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.telegramspam.R

class CurrentAccountFragment : Fragment() {

    companion object {
        fun newInstance() = CurrentAccountFragment()
    }

    private lateinit var viewModel: CurrentAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.current_account_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CurrentAccountViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
