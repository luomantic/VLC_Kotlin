package com.luomantic.vlc_kotlin.act

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import com.luomantic.vlc_kotlin.R
import kotlinx.android.synthetic.main.act_vlc.*
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import kotlin.math.roundToInt


class VlcAct : Activity() { // , IVLCVout.OnNewVideoLayoutListener // 毫无卵用
    private var videoHeight = 0
    private var videoWidth = 0
    private var videoVisibleHeight = 0
    private var videoVisibleWidth = 0
    private var videoSarSum = 0
    private var videoSarDen = 0

    private val surBestFit = 0 // 适应窗口大小
    private val surFitScreen = 1 // 适应屏幕
    private val surFill = 2 // 填充
    private val sur16to9 = 3
    private val sur4to3 = 4
    private val surOriginal = 5
    private var currentSize = surBestFit

    // 亲测走不到这个方法
//    /**
//     * This listener is called when the "android-display" "vout display" module request a new
//     * video layout. The implementation should take care of changing the surface
//     * LayoutsParams accordingly.
//     *
//     * If width and height are 0, LayoutParams should be reset to the initial state (MATCH_PARENT).
//     *
//     * By default, "android-display" is used when doing HW decoding and if Video and Subtitles
//     * surfaces are correctly attached.
//     *
//     * You could force "--vout=android-display" from LibVLC
//     * arguments if you want to use this module without subtitles.
//     *
//     * Otherwise, the "opengles2" module will be used (for SW and HW decoding) and this callback will always send a size of 0.
//     */
//    override fun onNewVideoLayout(
//        vlcVout: IVLCVout?,
//        width: Int,
//        height: Int,
//        visibleWidth: Int,
//        visibleHeight: Int,
//        sarNum: Int,
//        sarDen: Int
//    ) {
//        videoWidth = width
//        videoHeight = height
//        videoVisibleWidth = visibleWidth
//        videoVisibleHeight = visibleHeight
//        videoSarSum = sarNum
//        videoSarDen = sarDen
//
//        println(
//            "onNewVideoLayout: videoWidth:$videoWidth, videoHeight:$videoHeight, videoVisibleWidth:$videoVisibleWidth, videoVisibleHeight:$videoVisibleHeight, " +
//                    "videoSarSum:$videoSarSum, videoSarDen:$videoSarDen"
//        )
//        updateVideoSurface()
//    }

    private val videoPath = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"

    private val options = arrayListOf(
        ":file-caching=1024",
        ":network-caching=1024",
        ":live-caching=1024",
        ":sout-mux-caching=1024",
        ":codec=mediacodec,iomx,all"
    )
    private lateinit var libVlc: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vlcCount: IVLCVout
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_vlc)

//        libVlc = LibVLC(this, options)
        libVlc = LibVLC(this)
        mediaPlayer = MediaPlayer(libVlc)

        vlcCount = mediaPlayer.vlcVout
        vlcCount.setVideoView(videoSurface)
        vlcCount.attachViews()
        videoFrame.addOnLayoutChangeListener(onLayoutChangeListener)

//        mediaPlayer.setEventListener { event ->
//            print(event.toString())
//        }

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
//        val sw = window.decorView.width  // 小米6x：1080
//
//        //整个装饰窗口的高度, 这个高度, 可以代表着整个玻璃屏幕的高度. 这个高度包括: 状态烂的高度和导航栏的高度.
//        // (状态栏和导航栏通常叫做装饰窗口, 而ActionBar不属于装饰窗口)
//        val sh = window.decorView.height // 小米6x：2160

        val sw = videoFrame.width
        val sh = videoFrame.height

        if (sw * sh == 0) { // 检测非0
            return
        }

        mediaPlayer.vlcVout.setWindowSize(sw, sh) // 发送window的尺寸给native，视频默认居中显示，如果不设置，视频画面就会出现在左下角.
//
//        var lp = videoSurface.layoutParams
//        println("frame: 宽：$sw, 高：$sh")
//        println("surface: 宽：${videoSurface.width}, 高：${videoSurface.height}")
//
//        if (videoWidth * videoHeight == 0) { // 由于openGl的视频输出影响，用mediaPlayer的api来处理视频显示
//            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
//            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
//
//            videoSurface.layoutParams = lp
//
//            lp = videoFrame.layoutParams
//            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
//            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
//            videoFrame.layoutParams = lp
            changeMediaPlayerLayout(sw, sh)
//            return
//        }

//        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
//            mediaPlayer.aspectRatio = null
//            mediaPlayer.scale = 0f
//        }
//
//        var dw: Double = sw.toDouble()
//        var dh: Double = sh.toDouble()
//        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
//        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
//            dw = sh.toDouble()
//            dh = sw.toDouble()
//        }
//
//        // 计算纵横比
//        var ar by Delegates.notNull<Double>()
//        var vw by Delegates.notNull<Double>()
//        if (videoSarDen == videoSarSum) {
//            vw = videoVisibleWidth.toDouble()
//            ar = videoVisibleWidth / videoVisibleHeight.toDouble()
//        } else {
//            vw = videoVisibleWidth / videoVisibleHeight.toDouble()
//            ar = vw / videoVisibleHeight.toDouble()
//        }
//
//        // 计算显示纵横比
//        val dar = dw / dh
//        when (currentSize) {
//            surBestFit -> {
//                if (dar < ar) {
//                    dh = dw / ar
//                } else {
//                    dw = dh * ar
//                }
//            }
//            surFitScreen -> {
//                if (dar >= ar) {
//                    dh = dw / ar  // 横向
//                } else {
//                    dw = dh * ar // 纵向
//                }
//            }
//            surFill -> {
//
//            }
//            sur16to9 -> {
//                ar = 16.0 / 9.0
//                if (dar < ar) {
//                    dh = dw / ar
//                } else {
//                    dw = dh * ar
//                }
//            }
//            sur4to3 -> {
//                ar = 4.0 / 3.0
//                if (dar < ar) {
//                    dh = dw / ar
//                } else {
//                    dw = dh * ar
//                }
//            }
//            surOriginal -> {
//                dh = videoVisibleHeight.toDouble()
//                dw = vw
//            }
//        }
//
//        // 设置显示尺寸
//        lp.width = ceil(dw * videoWidth / videoVisibleWidth).toInt()
//        lp.height = ceil(dh * videoHeight / videoVisibleHeight).toInt()
//        videoSurface.layoutParams = lp
//
//        lp = videoFrame.layoutParams
//        lp.width = floor(dw).toInt()
//        lp.height = floor(dh).toInt()
//        videoFrame.layoutParams = lp
//
//        videoSurface.invalidate()
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
                val vTrack = mediaPlayer.currentVideoTrack
                vTrack?.let {
                    val videoSwap =
                        vTrack.orientation == Media.VideoTrack.Orientation.LeftBottom ||
                                vTrack.orientation == Media.VideoTrack.Orientation.RightTop

                    when (currentSize) {
                        surFitScreen -> {
                            var videoW = vTrack.width
                            var videoH = vTrack.height

                            if (videoSwap) {
                                val swap = videoW
                                videoW = videoH
                                videoH = swap
                            }

                            if (vTrack.sarNum != vTrack.sarDen) {
                                videoW = (videoW / videoH.toFloat()).roundToInt()
                            }
                            val ar = videoW / videoH.toFloat()
                            val dar = displayW / displayH

                            val scale = when {
                                dar >= ar -> displayW / videoW
                                else -> displayH / videoH
                            }
                            mediaPlayer.scale = scale.toFloat()
                            mediaPlayer.aspectRatio = null
                        }
                        else -> {
//                            mediaPlayer.aspectRatio = when (videoSwap) {
//                                true -> "$displayW:$displayH" // 横屏
//                                false -> "$displayH:$displayW" // 竖屏
//                            }

                            mediaPlayer.aspectRatio = "1080:1674"
                            println("$displayW:$displayH")
                            mediaPlayer.scale = 0f
                        }
                    }
                }
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