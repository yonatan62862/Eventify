package com.eventify.app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.eventify.app.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        view.findViewById<Button>(R.id.btnMyProfile).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_myProfileFragment)
        }

        view.findViewById<Button>(R.id.btnMyEvents).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_myEventsFragment)
        }

        view.findViewById<Button>(R.id.btnCreateEvent).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_createEventFragment)
        }
    }
}
