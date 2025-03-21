package com.eventify.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.eventify.app.databinding.FragmentEditEventBinding
import com.eventify.app.network.CloudinaryUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.util.*

class EditEventFragment : Fragment() {
    private lateinit var binding: FragmentEditEventBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var cloudinaryUploader: CloudinaryUploader
    private var eventId: String? = null
    private var imageUrl: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        cloudinaryUploader = CloudinaryUploader(requireContext())

        eventId = arguments?.getString("event_id")

        if (eventId == null) {
            Log.e("EditEventFragment", "Event ID is null!")
            Toast.makeText(requireContext(), "Error: Event ID not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        loadEventDetails(eventId!!)

        binding.btnChangeDate.setOnClickListener { showDatePicker() }
        binding.btnChangeTime.setOnClickListener { showTimePicker() }
        binding.btnSelectImage.setOnClickListener { openImageChooser() }
        binding.btnSaveEvent.setOnClickListener { saveEventUpdates() }
    }

    private fun loadEventDetails(eventId: String) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        firestore.collection("events").document(eventId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val ownerId = document.getString("ownerId")
                    if (FirebaseAuth.getInstance().currentUser?.uid != ownerId) {
                        Toast.makeText(requireContext(), "You are not authorized to edit this event.", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                        return@addOnSuccessListener
                    }

                    binding.etEventTitle.text = document.getString("name")?.toEditable() ?: "".toEditable()
                    binding.etLocation.text = document.getString("location")?.toEditable() ?: "".toEditable()
                    binding.btnChangeDate.text = document.getString("startDate")?.toEditable() ?: "".toEditable()
                    binding.btnChangeTime.text = document.getString("startTime")?.toEditable() ?: "".toEditable()

                    imageUrl = document.getString("imageUrl")

                    if (!imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(imageUrl).into(binding.eventImageView)
                    }
                } else {
                    Log.e("EditEventFragment", "No event found with ID: $eventId")
                    Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
            .addOnFailureListener {
                Log.e("EditEventFragment", "Failed to load event details", it)
                Toast.makeText(requireContext(), "Failed to load event details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            binding.btnChangeDate.text = "$day/${month + 1}/$year".toEditable()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, hour, minute ->
            binding.btnChangeTime.text = String.format("%02d:%02d", hour, minute).toEditable()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && data?.data != null) {
            val imageUri: Uri = data.data!!
            binding.eventImageView.setImageURI(imageUri)

            lifecycleScope.launch {
                val uploadedUrl = cloudinaryUploader.uploadImage(imageUri)
                if (uploadedUrl != null) {
                    imageUrl = uploadedUrl
                } else {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveEventUpdates() {
        val updatedData = mapOf(
            "name" to binding.etEventTitle.text.toString(),
            "location" to binding.etLocation.text.toString(),
            "startDate" to binding.btnChangeDate.text.toString(),
            "startTime" to binding.btnChangeTime.text.toString(),
            "imageUrl" to imageUrl
        )

        eventId?.let {
            firestore.collection("events").document(it)
                .update(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener {
                    Log.e("EditEventFragment", "Failed to update event", it)
                    Toast.makeText(requireContext(), "Failed to update event", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

