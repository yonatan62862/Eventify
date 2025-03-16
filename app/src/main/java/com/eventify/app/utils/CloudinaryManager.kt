package com.eventify.app.utils

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryManager {
    fun init(context: Context) {
        val config: HashMap<String, String> = hashMapOf(
            "cloud_name" to "dhmjcych3",
            "api_key" to "538911226192628",
            "api_secret" to "uUZMNMo9MIUjJyNBvuvbjso1j4Y"
        )
        MediaManager.init(context, config)
    }
}
