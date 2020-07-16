package com.example.telegramspam.ui.inviter

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.telegramspam.R

class InviterFragment : Fragment() {

    companion object {
        fun newInstance() = InviterFragment()
    }

    private lateinit var viewModel: InviterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.inviter_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(InviterViewModel::class.java)
        // TODO: Use the ViewModel
    }

}