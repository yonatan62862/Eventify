package com.eventify.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eventify.app.R
import com.eventify.app.adapters.EventAdapter
import com.eventify.app.viewmodel.MyEventsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar

class MyEventsFragment : Fragment() {
    private val viewModel: MyEventsViewModel by viewModels()
    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMyEvents)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = EventAdapter { eventId ->
            viewModel.deleteEvent(eventId)
            Snackbar.make(view, "Event deleted successfully", Snackbar.LENGTH_SHORT).show()
            viewModel.fetchUserEvents()
        }

        recyclerView.adapter = adapter

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return

        viewModel.fetchUserEvents()

        viewModel.localEvents.observe(viewLifecycleOwner) { localEventList ->
            if (localEventList.isNotEmpty()) {
                adapter.submitList(localEventList)
            }
        }

        viewModel.remoteEvents.observe(viewLifecycleOwner) { remoteEventList ->
            if (remoteEventList.isNotEmpty()) {
                adapter.submitList(remoteEventList)
            }
        }

    }
}
