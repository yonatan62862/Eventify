package com.eventify.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.eventify.app.data.local.EventEntity
import com.eventify.app.databinding.FragmentCreateEventBinding
import com.eventify.app.network.CloudinaryUploader
import com.eventify.app.viewmodel.MyEventsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class CreateEventFragment : Fragment() {

    private lateinit var binding: FragmentCreateEventBinding
    private lateinit var eventViewModel: MyEventsViewModel
    private val eventTypes = arrayOf("Wedding", "Birthday", "Party", "Company Event")

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.eventImageView.setImageURI(it)
            binding.eventImageView.visibility = View.VISIBLE
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        eventViewModel = ViewModelProvider(this).get(MyEventsViewModel::class.java)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, eventTypes)
        binding.spinnerEventType.adapter = adapter

        binding.btnStartDate.setOnClickListener { showDatePicker(binding.btnStartDate) }
        binding.btnStartTime.setOnClickListener { showTimePicker(binding.btnStartTime) }
        binding.btnEndDate.setOnClickListener { showDatePicker(binding.btnEndDate) }
        binding.btnEndTime.setOnClickListener { showTimePicker(binding.btnEndTime) }
        binding.btnSelectImage.setOnClickListener { pickImageLauncher.launch("image/*") }

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

    private fun fetchUserIdsFromEmails(emails: List<String>, callback: (List<String>) -> Unit) {
        if (emails.isEmpty()) {
            callback(emptyList())
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereIn("email", emails)
            .get()
            .addOnSuccessListener { documents ->
                val invitedUserIds = documents.map { it.id }
                callback(invitedUserIds)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch user IDs", e)
                callback(emptyList())
            }
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
        val ownerId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"

        val invitedEmails = binding.emailInput.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        fetchUserIdsFromEmails(invitedEmails) { invitedUserIds ->
            lifecycleScope.launch {
                try {
                    val imageUploadDeferred = async(Dispatchers.IO) { uploadImageToCloudinary() }
                    val imageUrl = imageUploadDeferred.await()

                    Log.d("CreateEventFragment", "Image uploaded: $imageUrl")

                    val event = EventEntity(
                        id = UUID.randomUUID().toString(),
                        name = title,
                        eventType = eventType,
                        description = description,
                        startDate = startDate,
                        startTime = startTime,
                        endDate = endDate,
                        endTime = endTime,
                        location = location,
                        ownerId = ownerId,
                        invitedUsers = invitedUserIds,
                        imageUrl = imageUrl
                    )

                    val roomSaveDeferred = async { eventViewModel.insertEvent(event) }
                    val firestoreSaveDeferred = async { saveEventToFirestore(event) }

                    roomSaveDeferred.await()
                    firestoreSaveDeferred.await()

                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveEvent.isEnabled = true

                    Toast.makeText(requireContext(), "Event saved successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } catch (e: Exception) {
                    Log.e("CreateEventFragment", "Error saving event", e)
                    Toast.makeText(requireContext(), "Failed to save event", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveEvent.isEnabled = true
                }
            }
        }
    }

    private suspend fun saveEventToFirestore(event: EventEntity) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events")
            .document(event.id)
            .set(event)
            .addOnSuccessListener {
                Log.d("CreateEventFragment", "Event saved to Firestore successfully")
            }
            .addOnFailureListener { e ->
                Log.e("CreateEventFragment", "Failed to save event to Firestore", e)
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

    private suspend fun uploadImageToCloudinary(): String {
        return selectedImageUri?.let {
            Log.d("CreateEventFragment", "Uploading image to Cloudinary...")
            return@let CloudinaryUploader(requireContext()).uploadImage(it)
        } ?: ""
    }
}
