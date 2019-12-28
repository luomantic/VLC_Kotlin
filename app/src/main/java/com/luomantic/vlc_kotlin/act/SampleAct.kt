package com.luomantic.vlc_kotlin.act

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luomantic.vlc_kotlin.R
import kotlinx.android.synthetic.main.act_sample.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class SampleAct : AppCompatActivity() {
    private val useTextureView = false
    private val enableSubTitle = true
    private val assertFileName = "bbb.m4v"

    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_sample)

        val args = arrayListOf("-vvv")
        libVLC = LibVLC(application, args)
        mediaPlayer = MediaPlayer(libVLC)
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer?.release()
        libVLC?.release()
    }

    override fun onStart() {
        super.onStart()

        mediaPlayer?.attachViews(vlcLayout, null, enableSubTitle, useTextureView)

        val media = Media(libVLC, assets.openFd(assertFileName))
        mediaPlayer?.media = media

        media.release()

        mediaPlayer?.play()
    }

    override fun onStop() {
        super.onStop()

        mediaPlayer?.stop()
        mediaPlayer?.detachViews()
    }
}