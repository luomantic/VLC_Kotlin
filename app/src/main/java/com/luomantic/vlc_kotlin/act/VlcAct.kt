package com.luomantic.vlc_kotlin.act

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.luomantic.vlc_kotlin.R
import kotlinx.android.synthetic.main.act_vlc.*
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

/**
 *
设置视频窗口大小的办法
mediaPlayer.vlcVout.setWindowSize(sw, sh) // 发送surfaceView的尺寸给native, 如果不发送的话，视频默认会显示surfaceView在左下角
// 发送尺寸给native以后，视频画面默认出现在surfaceView的中间

① bsetFit
mediaPlayer.aspectRatio = null
mediaPlayer.scale = 0f

② 指定大小
mediaPlayer.aspectRatio = "16:9"
mediaPlayer.scale = 0f

③ 原始大小
mediaPlayer.aspectRatio = null
mediaPlayer.scale = 1f

 */
class VlcAct : Activity() {
    private val surBestFit = 0 // 适应窗口大小
    private val surFitScreen = 1 // 适应屏幕
    private val surFill = 2 // 填充
    private val sur16to9 = 3
    private val sur4to3 = 4
    private val surOriginal = 5
    private var currentSize = surBestFit

    private val videoPath = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"

    private lateinit var libVlc: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vlcCount: IVLCVout
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_vlc)

        libVlc = LibVLC(this)
        mediaPlayer = MediaPlayer(libVlc)

        vlcCount = mediaPlayer.vlcVout
        vlcCount.setVideoView(videoSurface)
        vlcCount.attachViews()
        videoFrame.addOnLayoutChangeListener(onLayoutChangeListener)

        btBestFit.setOnClickListener {
            currentSize = surBestFit
            handler.post {
                updateVideoSurface()
            }
        }
        btFitScreen.setOnClickListener {
            currentSize = surFitScreen
            handler.post {
                updateVideoSurface()
            }
        }
        btFill.setOnClickListener {
            currentSize = surFill
            handler.post {
                updateVideoSurface()
            }
        }
        bt16to9.setOnClickListener {
            currentSize = sur16to9
            handler.post {
                updateVideoSurface()
            }
        }
        bt4to3.setOnClickListener {
            currentSize = sur4to3
            updateVideoSurface()
        }
        btOriginal.setOnClickListener {
            currentSize = surOriginal
            updateVideoSurface()
        }

        btNet.setOnClickListener {
            println("播放网络")
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            val media = Media(libVlc, Uri.parse(videoPath))
            mediaPlayer.media = media
            media.release()
            mediaPlayer.play()
        }
        btLocal.setOnClickListener {
            println("播放本地")
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }

            val media = Media(libVlc, assets.openFd("bbb.m4v"))
            mediaPlayer.media = media
            media.release()
            mediaPlayer.play()
        }
        btPause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                println("暂停")
                mediaPlayer.pause()
            }
        }
        btPlay.setOnClickListener {
            println("播放")
            mediaPlayer.play()
        }
    }

    private val onLayoutChangeListener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View?,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                handler.removeCallbacks(runnable)
                handler.post(runnable)
            }
        }

        val runnable = Runnable {
            run {
                updateVideoSurface()
            }
        }
    }

    fun updateVideoSurface() {
        val sw = videoFrame.width
        val sh = videoFrame.height

        if (sw * sh == 0) { // 检测非0
            return
        }

        mediaPlayer.vlcVout.setWindowSize(sw, sh) // 发送window的尺寸给native, 如果不发送的话，视频默认会显示surfaceView在左下角

        changeMediaPlayerLayout(sw, sh)
    }

    /**
     * 使用MediaPlayer的api改变video播放窗口的大小
     */
    private fun changeMediaPlayerLayout(displayW: Int, displayH: Int) {
        when (currentSize) {
            surBestFit -> {
                mediaPlayer.aspectRatio = null
                mediaPlayer.scale = 0f // 0表示填充整个屏幕，如果scale为1，表示显示比例跟解码出来的视频大小一致
                println("bestFit")
            }
            surFitScreen -> {
            } // 留到surfaceFill一并处理
            surFill -> {
                mediaPlayer.aspectRatio = "1080:1674"
                println("$displayW:$displayH")
                mediaPlayer.scale = 0f
            }
            sur16to9 -> {
                mediaPlayer.aspectRatio = "16:9"
                mediaPlayer.scale = 0f
            }
            sur4to3 -> {
                mediaPlayer.aspectRatio = "4:3"
                mediaPlayer.scale = 0f
            }
            surOriginal -> {
                mediaPlayer.aspectRatio = null
                mediaPlayer.scale = 1f
            }
        }
    }

    override fun onStop() {
        super.onStop()
        videoFrame.removeOnLayoutChangeListener(onLayoutChangeListener)
        mediaPlayer.stop()
        vlcCount.detachViews()

        if (isFinishing) {
            mediaPlayer.release()
            libVlc.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        libVlc.release()
    }
}