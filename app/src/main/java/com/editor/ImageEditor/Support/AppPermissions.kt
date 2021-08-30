package com.editor.ImageEditor.Support

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AppPermissions(private val context: Context) {
    private val STORAGE_PERMISSION_CODE = 1

    // It checks self permissions
    fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            askForPermissions()
        }
    }

    // It asks for permissions if not granted already
    private fun askForPermissions() {
        AlertDialog.Builder(context)
            .setTitle("Permissions are needed")
            .setMessage("Few permissions are needed to save images")
            .setCancelable(false)
            .setPositiveButton(
                "Ok"
            ){ dialogInterface, i ->
                ActivityCompat.requestPermissions(
                    context as Activity, arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), STORAGE_PERMISSION_CODE
                )
            }.show()
    }
}