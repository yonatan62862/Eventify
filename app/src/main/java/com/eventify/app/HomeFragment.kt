package com.eventify.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

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

        view.findViewById<Button>(R.id.btnCosts).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_costsFragment)
        }

        view.findViewById<Button>(R.id.btnEventUpdates).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_eventUpdatesFragment)
        }
    }
}
