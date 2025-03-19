package com.eventify.app.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eventify.app.R
import com.eventify.app.adapters.ApiEventAdapter
import com.eventify.app.network.GoogleEventsResponse
import com.eventify.app.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiEventFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private val apiKey = "AIzaSyB0ReNflFFuzHDxp3bWR0ns_2OFk08qkdM"
    private val calendarId = "usa__en@holiday.calendar.google.com"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_api_event, container, false)
        recyclerView = view.findViewById(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        fetchEvents()
        return view
    }

    private fun fetchEvents() {
        RetrofitClient.instance.getEvents(calendarId, apiKey).enqueue(object : Callback<GoogleEventsResponse> {
            override fun onResponse(call: Call<GoogleEventsResponse>, response: Response<GoogleEventsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        recyclerView.adapter = ApiEventAdapter(requireContext(), it.items)
                    }
                } else {
                    Log.e("ApiEventFragment", "API Response Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GoogleEventsResponse>, t: Throwable) {
                Log.e("ApiEventFragment", "API Request Failed", t)
            }
        })
    }
}
