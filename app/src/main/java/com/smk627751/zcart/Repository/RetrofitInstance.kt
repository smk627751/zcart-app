package com.smk627751.zcart.Repository

import android.util.Log
import com.smk627751.zcart.dto.RequestBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface Api{
    @POST("/send")
    suspend fun sendNotification(@Body requestBody: RequestBody): Response<ResponseBody>
}
object RetrofitInstance {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    private val retrofit by lazy {  Retrofit.Builder()
        .baseUrl("https://fcm-nofitication.onrender.com")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    }
    private val api by lazy { retrofit.create(Api::class.java) }

    suspend fun sendNotification(requestBody: RequestBody) {
        try {
            val response = api.sendNotification(requestBody)
            if (response.isSuccessful) {
                Log.i("uuid", "Notification sent successfully")
            } else {
                throw Exception("Failed to send notification: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            throw Exception("Error sending notification: ${e.message}")
        }
    }
}