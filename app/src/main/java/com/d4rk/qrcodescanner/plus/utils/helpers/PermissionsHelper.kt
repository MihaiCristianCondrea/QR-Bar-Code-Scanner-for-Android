package com.d4rk.qrcodescanner.plus.utils.helpers

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsHelper {
    fun requestPermissions(activity : AppCompatActivity , permissions : Array<out String> , requestCode : Int) {
        if (areAllPermissionsGranted(activity , permissions)) {
            activity.onRequestPermissionsResult(
                requestCode , permissions , IntArray(permissions.size)
            )
            return
        }
        val notGrantedPermissions = permissions.filterNot { isPermissionGranted(activity , it) }
        ActivityCompat.requestPermissions(activity , notGrantedPermissions.toTypedArray() , requestCode)
    }

    fun requestNotGrantedPermissions(activity : AppCompatActivity , permissions : Array<out String> , requestCode : Int) {
        val notGrantedPermissions = permissions.filterNot { isPermissionGranted(activity , it) }
        if (notGrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity , notGrantedPermissions.toTypedArray() , requestCode)
        }
    }

    fun areAllPermissionsGranted(context : Context , permissions : Array<out String>) : Boolean {
        var allGranted = true
        permissions.forEach { permission ->
            if (isPermissionGranted(context , permission).not()) {
                allGranted = false
            }
        }
        return allGranted
    }

    fun areAllPermissionsGranted(grantResults : IntArray) : Boolean {
        var allGranted = true
        grantResults.forEach { result ->
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
            }
        }
        return allGranted
    }

    private fun isPermissionGranted(context : Context , permission : String) : Boolean {
        return ContextCompat.checkSelfPermission(context , permission) == PackageManager.PERMISSION_GRANTED
    }
}