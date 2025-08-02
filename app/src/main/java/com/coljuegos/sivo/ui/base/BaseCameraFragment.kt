package com.coljuegos.sivo.ui.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.coljuegos.sivo.utils.CameraHelper

abstract class BaseCameraFragment : Fragment() {

    private lateinit var cameraHelper: CameraHelper
    private lateinit var cameraReceiver: BroadcastReceiver

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCameraHelper()
        registerCameraReceiver()
    }

    private fun setupCameraHelper() {
        cameraHelper = CameraHelper(this) { imageUri ->
            handleCapturedImage(imageUri)
        }
    }

    private fun registerCameraReceiver() {
        cameraReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.coljuegos.sivo.CAMERA_ACTION") {
                    cameraHelper.showCameraOptions()
                }
            }
        }

        ContextCompat.registerReceiver(
            requireContext(),
            cameraReceiver,
            IntentFilter("com.coljuegos.sivo.CAMERA_ACTION"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    abstract fun handleCapturedImage(imageUri: Uri)

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            requireContext().unregisterReceiver(cameraReceiver)
        } catch (e: Exception) {
            // Receiver ya fue desregistrado
        }
    }

}