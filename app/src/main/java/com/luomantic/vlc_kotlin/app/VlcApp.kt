package com.luomantic.vlc_kotlin.app

import android.app.Application
import com.blankj.utilcode.util.Utils

class VlcApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Utils.init(this)
    }
}