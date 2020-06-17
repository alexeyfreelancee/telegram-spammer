package com.example.telegramspam.ui.current_chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.example.telegramspam.ACCOUNT_ID
import com.example.telegramspam.CHAT_ID
import com.example.telegramspam.FILE_ID
import com.example.telegramspam.R
import com.example.telegramspam.adapters.MessageListAdapter
import com.example.telegramspam.databinding.CurrentChatFragmentBinding
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class CurrentChatFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val factory by instance<CurrentChatViewModelFactory>()
    private  var binding: CurrentChatFragmentBinding? = null
    private lateinit var adapter: MessageListAdapter
    private lateinit var viewModel: CurrentChatViewModel
    private lateinit var layoutManager: LinearLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(binding!=null){
            return binding?.root
        }
        viewModel = ViewModelProvider(this, factory).get(CurrentChatViewModel::class.java)

        binding = CurrentChatFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = viewModel
        }

        viewModel.setupChat(
            arguments?.getLong(CHAT_ID, 0) ?: 0,
            arguments?.getInt(ACCOUNT_ID, 0) ?: 0
        )
        initObservers()
        setupMessagesList()
        KeyboardVisibilityEvent.setEventListener(activity){ if(it)scrollToBottom()}
        return binding?.root
    }

    private fun initObservers(){
        viewModel.toast.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                Toast.makeText(requireContext(), it.peekContent(), Toast.LENGTH_SHORT).show()
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
                startActivityForResult(Intent.createChooser(intent, "Прикрепить файлы"), 2)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data!=null) {
            viewModel.fileAttached(data, requireContext())
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)
        viewModel.openPhoto.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                navController.navigate(R.id.action_currentChatFragment_to_viewPhotoFragment, bundleOf(
                    FILE_ID to it.peekContent()
                ))
            }
        })
    }

    private fun setupMessagesList() {
        layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        adapter = MessageListAdapter(viewModel)
        binding?.messageList?.adapter = adapter
        binding?.messageList?.layoutManager = layoutManager
        viewModel.messages.observe(viewLifecycleOwner, Observer {
            adapter.fetchList(it)
        })
        viewModel.scrollEvent.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                it.peekContent()
                scrollToBottom()
            }
        })
    }

    //android recyclerview chat anchor bottom
    private fun scrollToBottom() {
        layoutManager.scrollToPosition(0)
    }


}