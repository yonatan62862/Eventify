package com.eventify.app.network

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.awaitResponse
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CloudinaryUploader(private val context: Context) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.cloudinary.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val cloudinaryApi = retrofit.create(CloudinaryApi::class.java)

    suspend fun uploadImage(imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val imageFile = uriToFile(imageUri)
                val requestFile = imageFile.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
                val uploadPreset = "Eventify".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = cloudinaryApi.uploadImage(
                    "v1_1/dhmjcych3/image/upload",
                    filePart,
                    uploadPreset
                ).awaitResponse()

                if (response.isSuccessful) {
                    return@withContext response.body()?.secure_url
                } else {
                    Log.e("Cloudinary", "Failed to upload: ${response.errorBody()?.string()}")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("Cloudinary", "Upload error: ${e.localizedMessage}")
                return@withContext null
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return file
    }
}
