package com.eventify.app.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url
import retrofit2.Call

interface CloudinaryApi {
    @Multipart
    @POST
    fun uploadImage(
        @Url url: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): Call<CloudinaryResponse>
}
