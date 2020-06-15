package com.example.telegramspam.ui.view_photo

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.telegramspam.FILE_ID
import com.example.telegramspam.R
import com.example.telegramspam.data.telegram.TelegramClientUtil
import com.example.telegramspam.ui.MainActivity
import com.example.telegramspam.utils.gone
import com.example.telegramspam.utils.log
import com.example.telegramspam.utils.visible
import kotlinx.coroutines.*


class ViewPhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_photo, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fileId = arguments?.getInt(FILE_ID, 0) ?: 0
        val photo = view.findViewById<ImageView>(R.id.photo)
        val progress = view.findViewById<ProgressBar>(R.id.progress)
        progress.visible()
        CoroutineScope(Dispatchers.IO).launch {

            val file = TelegramClientUtil.downloadFile(fileId)

            withContext(Dispatchers.Main){
                progress.gone()
                Glide.with(requireContext())
                    .load(file)
                    .into(photo)
            }
        }
    }



}