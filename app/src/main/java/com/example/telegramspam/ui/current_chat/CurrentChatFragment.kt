package com.example.telegramspam.ui.current_chat

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.telegramspam.ACCOUNT_ID
import com.example.telegramspam.CHAT_ID
import com.example.telegramspam.adapters.MessageListAdapter
import com.example.telegramspam.databinding.CurrentChatFragmentBinding
import com.example.telegramspam.utils.log
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class CurrentChatFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory by instance<CurrentChatViewModelFactory>()
    private lateinit var binding: CurrentChatFragmentBinding
    private lateinit var adapter: MessageListAdapter
    private lateinit var viewModel: CurrentChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(CurrentChatViewModel::class.java)
        binding = CurrentChatFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = viewModel
        }

        viewModel.setupChat(
            arguments?.getLong(CHAT_ID, 0) ?: 0,
            arguments?.getInt(ACCOUNT_ID, 0) ?: 0
        )
        setupMessagesList()
        KeyboardVisibilityEvent.setEventListener(activity){ if(it)scrollToBottom()}
        return binding.root
    }




    private fun setupMessagesList() {
        val layoutManager = LinearLayoutManager(requireContext())
        adapter = MessageListAdapter(viewModel)
        binding.messageList.adapter = adapter
        binding.messageList.layoutManager = layoutManager
        viewModel.messages.observe(viewLifecycleOwner, Observer {
            log("observed list of ${it.size} messages")
            adapter.submitList(it)
            if(layoutManager.findFirstVisibleItemPosition() < 10){
                scrollToBottom()
            }
        })
    }

    private fun scrollToBottom() {
        binding.messageList.layoutManager?.scrollToPosition(adapter.itemCount - 1)
    }

}