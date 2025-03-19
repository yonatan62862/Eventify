package com.eventify.app.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleEventsApi {
    @GET("calendars/{calendarId}/events")
    fun getEvents(
        @Path("calendarId") calendarId: String,
        @Query("key") apiKey: String
    ): Call<GoogleEventsResponse>
}