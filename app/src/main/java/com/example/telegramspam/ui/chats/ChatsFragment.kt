package com.example.telegramspam.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.telegramspam.ACCOUNT
import com.example.telegramspam.ACCOUNT_ID
import com.example.telegramspam.CHAT_ID
import com.example.telegramspam.R
import com.example.telegramspam.adapters.ChatListAdapter
import com.example.telegramspam.databinding.ChatFragmentBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ChatsFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory by instance<ChatsViewModelFactory>()
    private lateinit var binding: ChatFragmentBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)
        binding = ChatFragmentBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        viewModel.setupAccount(arguments?.getInt(ACCOUNT_ID, 0) ?: 0)
        setupChatList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)
        viewModel.openChat.observe(viewLifecycleOwner, Observer {
            if (!it.hasBeenHandled) {
                navController.navigate(
                    R.id.action_chatFragment_to_currentChatFragment, bundleOf(
                        CHAT_ID to it.peekContent(),
                        ACCOUNT_ID to viewModel.accountId
                    )
                )
            }
        })
    }



    private fun setupChatList() {
        adapter = ChatListAdapter(viewModel)
        binding.chatList.adapter = adapter
        viewModel.chats.observe(viewLifecycleOwner, Observer {
            adapter.fetchList(it)
        })
    }
}