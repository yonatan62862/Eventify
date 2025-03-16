package com.eventify.app

import android.app.Application
import com.eventify.app.utils.CloudinaryManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CloudinaryManager.init(this)
    }
}
