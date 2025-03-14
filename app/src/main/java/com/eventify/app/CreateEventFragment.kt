package com.eventify.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.eventify.app.data.local.EventEntity
import com.eventify.app.databinding.FragmentCreateEventBinding
import com.eventify.app.ViewModel.EventViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class CreateEventFragment : Fragment() {

    private lateinit var binding: FragmentCreateEventBinding
    private lateinit var eventViewModel: EventViewModel
    private val eventTypes = arrayOf("Wedding", "Birthday", "Party", "Company Event")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, eventTypes)
        binding.spinnerEventType.adapter = adapter

        binding.btnStartDate.setOnClickListener { showDatePicker(binding.btnStartDate) }
        binding.btnStartTime.setOnClickListener { showTimePicker(binding.btnStartTime) }
        binding.btnEndDate.setOnClickListener { showDatePicker(binding.btnEndDate) }
        binding.btnEndTime.setOnClickListener { showTimePicker(binding.btnEndTime) }

        binding.btnSaveEvent.setOnClickListener {
            saveEvent()
        }

        return binding.root
    }

    private fun validateInputs(): Boolean {
        val title = binding.etEventTitle.text.toString().trim()
        val description = binding.etEventDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val startDate = binding.btnStartDate.text.toString()
        val startTime = binding.btnStartTime.text.toString()
        val endDate = binding.btnEndDate.text.toString()
        val endTime = binding.btnEndTime.text.toString()

        var isValid = true

        if (title.isEmpty()) {
            binding.etEventTitle.error = "Event title is required"
            isValid = false
        }

        if (description.isEmpty()) {
            binding.etEventDescription.error = "Event description is required"
            isValid = false
        }

        if (location.isEmpty()) {
            binding.etLocation.error = "Event location is required"
            isValid = false
        }

        if (startDate == "DD/MM/YY") {
            Toast.makeText(requireContext(), "Please select a start date", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (startTime == "12:00 AM") {
            Toast.makeText(requireContext(), "Please select a start time", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (endDate == "DD/MM/YY") {
            Toast.makeText(requireContext(), "Please select an end date", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (endTime == "12:00 AM") {
            Toast.makeText(requireContext(), "Please select an end time", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun saveEvent() {
        if (!validateInputs()) return

        binding.btnSaveEvent.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val title = binding.etEventTitle.text.toString().trim()
        val description = binding.etEventDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val startDate = binding.btnStartDate.text.toString()
        val startTime = binding.btnStartTime.text.toString()
        val endDate = binding.btnEndDate.text.toString()
        val endTime = binding.btnEndTime.text.toString()
        val eventType = binding.spinnerEventType.selectedItem.toString()
        val ownerEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown"

        val invitedEmails = binding.emailInput.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val eventId = UUID.randomUUID().toString()
        val event = EventEntity(eventId, title, eventType, description, startDate, startTime, endDate, endTime, location, ownerEmail, invitedEmails)

        lifecycleScope.launch {
            try {
                eventViewModel.insertEvent(event)

                binding.progressBar.visibility = View.GONE
                binding.btnSaveEvent.isEnabled = true

                Toast.makeText(requireContext(), "Event saved successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to save event", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnSaveEvent.isEnabled = true
            }
        }
    }


    private fun showDatePicker(button: Button) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            button.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day)

        datePicker.show()
    }

    private fun showTimePicker(button: Button) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            button.text = String.format("%02d:%02d", selectedHour, selectedMinute)
        }, hour, minute, true)

        timePicker.show()
    }
}
