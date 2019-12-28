package com.luomantic.vlc_kotlin.act

import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.luomantic.vlc_kotlin.R
import kotlinx.android.synthetic.main.act_main.*
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainAct : AppCompatActivity(), EasyPermissions.PermissionCallbacks, SurfaceHolder.Callback,
    IVLCVout.OnNewVideoLayoutListener {
    override fun onNewVideoLayout(
        vlcVout: IVLCVout?,
        width: Int,
        height: Int,
        visibleWidth: Int,
        visibleHeight: Int,
        sarNum: Int,
        sarDen: Int
    ) {
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        println("surfaceChanged")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) { // 拒绝权限且点击了不再提示
            AppSettingsDialog.Builder(this).build().show() // 跳转应用设置页面
        } else {
            EasyPermissions.requestPermissions(this, "再次申请权限，请同意", 1, *permissions)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        initMain()
    }

    private var permissions = arrayOf(
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

//        if (EasyPermissions.hasPermissions(this, *permissions)) {
//            initMain()
//        } else {
//            EasyPermissions.requestPermissions(this, "申请权限，请同意", 0, *permissions)
//        }

        initMain()
    }

    private var mediaPlayer: MediaPlayer? = null
    private val assertFileName = "bbb.m4v"

    private fun initMain() {
        bt_new.setOnClickListener {
            startActivity(Intent(this, VlcAct::class.java))
            this.finish()
        }

//        surface.setZOrderOnTop(true)
//        surface.setZOrderMediaOverlay(true)
//        surface.holder.addCallback(this)
//        surface.holder.setFixedSize(ScreenUtils.getScreenWidth(), SizeUtils.dp2px(200F))

        val options = arrayListOf<String>()
        options.add(":file-caching=1024")     // 文件缓存
        options.add(":network-caching=1024")  // 网络缓存
        options.add(":live-caching=1024")     // 直播缓存
        options.add(":sout-mux-caching=1024") // 输出缓存
        options.add(":codec=mediacodec,iomx,all") // 硬解码, 混合解码

        val libVLC = LibVLC(this, options)

        mediaPlayer = MediaPlayer(libVLC)

        mediaPlayer?.aspectRatio = null
        mediaPlayer?.scale = 0F
        surface.invalidate()

        if (mediaPlayer!!.isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }

        mediaPlayer = MediaPlayer(libVLC)
//        val url = "https://cctvcnch5ca.v.wscdns.com/live/cctv8_2/index.m3u8?contentid=2820180516001"
        val url = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"
//        val url = "rtsp://admin:zxh12345@192.168.0.216:554/h264/ch1/main/av_stream"
//        val url = "rtmp://media3.sinovision.net:1935/live/livestream"

//        mediaPlayer?.vlcVout?.setVideoSurface(surface.holder.surface, surface.holder)
        mediaPlayer?.vlcVout?.setVideoView(surface)
        mediaPlayer?.vlcVout?.attachViews()

        surface.post {
            val width = surface.measuredWidth
            val height = surface.measuredHeight

            println("$width + $height")

            mediaPlayer?.vlcVout?.setWindowSize(width, height)
            mediaPlayer?.aspectRatio = "width:height"
            mediaPlayer?.scale = 0F
            println("onCreate")

//            val media = Media(libVLC, Uri.parse(url))
            val media = Media(libVLC, assets.openFd(assertFileName))
            mediaPlayer?.media = media
            media.release()

            mediaPlayer?.play()
        }
    }

    override fun onPause() {
        super.onPause()
        println("onPause: 暂停")
//        this.mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        println("onResume: 播放")
//        this.mediaPlayer?.play()
//        mediaPlayer?.updateVideoSurfaces()
    }

    override fun onStop() {
        super.onStop()
//        mediaPlayer?.stop()
//        mediaPlayer?.detachViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy: 停止播放")
        this.mediaPlayer?.release()
        this.mediaPlayer = null
    }

}
