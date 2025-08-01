package com.coljuegos.sivo.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.coljuegos.sivo.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(
    private val fragment: Fragment,
    private val onImageCaptured: (Uri) -> Unit
) {

    private var imageUri: Uri? = null

    private val cameraPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showError(fragment.getString(R.string.camera_permission_required))
        }
    }

    private val galleryPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            showError(fragment.getString(R.string.storage_permission_required))
        }
    }

    private val cameraLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            onImageCaptured(imageUri!!)
        }
    }

    private val galleryLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageCaptured(it) }
    }

    fun showCameraOptions() {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(fragment.getString(R.string.choose_photo_source))
            .setItems(
                arrayOf(
                    fragment.getString(R.string.camera),
                    fragment.getString(R.string.gallery)
                )
            ) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> checkGalleryPermissionAndOpen()
                }
            }
            .setNegativeButton(fragment.getString(R.string.cancel), null)
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkGalleryPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            else -> {
                galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        imageUri = FileProvider.getUriForFile(
            fragment.requireContext(),
            "${fragment.requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(imageUri)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = fragment.requireContext().getExternalFilesDir("Pictures")
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

}