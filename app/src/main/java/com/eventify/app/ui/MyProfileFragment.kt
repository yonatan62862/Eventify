package com.eventify.app.ui

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.eventify.app.databinding.FragmentMyProfileBinding
import com.eventify.app.network.CloudinaryUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class MyProfileFragment : Fragment() {
    private lateinit var binding: FragmentMyProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ImageView
    private lateinit var cloudinaryUploader: CloudinaryUploader

    private val REQUEST_STORAGE_PERMISSION = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        profileImageView = binding.profileImageView
        cloudinaryUploader = CloudinaryUploader(requireContext())

        val user = auth.currentUser
        user?.let {
            binding.tvUserEmail.text = it.email ?: "No Email"
            binding.etUserName.setText(it.displayName ?: "")
            loadUserProfile(it.uid)
        }

        binding.btnUploadImage.setOnClickListener { checkPermissionAndOpenGallery() }
        binding.btnSaveProfile.setOnClickListener { saveUserProfile() }
    }

    private fun loadUserProfile(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "No Name"
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""

                    binding.etUserName.setText(name)
                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileImageView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openImageChooser()
        } else {
            requestPermissions(arrayOf(permission), REQUEST_STORAGE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImageChooser()
        } else {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                profileImageView.setImageURI(it)
                uploadImageToCloudinary(it)
            } ?: Toast.makeText(requireContext(), "Failed to get image", Toast.LENGTH_SHORT).show()
        }

    private fun openImageChooser() {
        imagePickerLauncher.launch("image/*")
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        lifecycleScope.launch {
            val imageUrl = cloudinaryUploader.uploadImage(imageUri)
            if (imageUrl != null) {
                saveImageUrlToFirestore(imageUrl)
                saveImageToGallery(imageUrl)
            } else {
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val user = auth.currentUser ?: return
        firestore.collection("users").document(user.uid)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Picasso.get().load(imageUrl).into(profileImageView)
                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageToGallery(imageUrl: String) {
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle("Profile Picture")
            .setDescription("Downloading profile picture")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "profile_picture.jpg")

        val manager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

        Toast.makeText(requireContext(), "Image saved to gallery", Toast.LENGTH_SHORT).show()
    }

    private fun saveUserProfile() {
        val user = auth.currentUser ?: return
        firestore.collection("users").document(user.uid)
            .update("name", binding.etUserName.text.toString())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}
