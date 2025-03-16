package com.eventify.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.eventify.app.R
import com.eventify.app.adapters.GalleryAdapter

class GalleryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewGallery)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        fetchImagesFromCloudinary()
    }

    private fun fetchImagesFromCloudinary() {
        val cloudName = "YOUR_CLOUD_NAME"
        val url = "https://res.cloudinary.com/$cloudName/image/list/profile_pictures.json"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val resources = response.getJSONArray("resources")
                val imageList = mutableListOf<String>()

                for (i in 0 until resources.length()) {
                    val imageUrl = "https://res.cloudinary.com/$cloudName/image/upload/" + resources.getJSONObject(i).getString("public_id") + ".jpg"
                    imageList.add(imageUrl)
                }

                recyclerView.adapter = GalleryAdapter(imageList)
            },
            { error ->
                Toast.makeText(requireContext(), "Failed to load images: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(requireContext()).add(request)
    }
}
