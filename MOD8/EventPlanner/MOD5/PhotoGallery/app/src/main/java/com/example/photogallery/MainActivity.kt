package com.example.photogallery

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Images.Media
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val REQUEST_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        val spanCount = 4 // Number of columns
        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing))

        requestStoragePermission()
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_PERMISSION)
        } else {
            loadPhotos()
        }
    }

    private fun loadPhotos() {
        val photos = mutableListOf<Photo>()
        val projection = arrayOf(Media._ID)
        val sortOrder = "${Media.DATE_TAKEN} DESC"

        try {
            val cursor = contentResolver.query(
                Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(Media._ID)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val contentUri = Media.EXTERNAL_CONTENT_URI.buildUpon()
                        .appendPath(id.toString()).build()
                    photos.add(Photo(contentUri.toString()))
                }
            }
        } catch (e: Exception) {
            // Handle exception
        }

        recyclerView.adapter = PhotoAdapter(photos)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPhotos()
            } else {
                // Handle permission denied
            }
        }
    }
}

class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount

        if (position >= spanCount) {
            outRect.top = spacing
        }
    }
}