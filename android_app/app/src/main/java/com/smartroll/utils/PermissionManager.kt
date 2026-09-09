package com.smartroll.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

object PermissionManager {

    fun getRequiredBlePermissions(role: String? = null): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (role) {
                "teacher" -> arrayOf(
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
                "student" -> arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
                else -> arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            }
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    fun hasPermissions(context: Context, role: String? = null): Boolean {
        return getRequiredBlePermissions(role).all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions(activity: Activity, requestCode: Int, role: String? = null) {
        ActivityCompat.requestPermissions(activity, getRequiredBlePermissions(role), requestCode)
    }

    /**
     * Checks if system-level Location Services (GPS/Network) are enabled.
     * Crucial for Android 11 and below (API <= 30), where BLE scanning silently returns nothing if OFF.
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    /**
     * Verifies system location services on Android 11 and below.
     * Prompts the user to turn on Location if it's disabled.
     * Returns true if location is enabled (or not required on this Android version).
     */
    fun checkAndPromptLocation(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            if (!isLocationEnabled(activity)) {
                AlertDialog.Builder(activity)
                    .setTitle("Location Services Required")
                    .setMessage("On Android 11 and below, system Location Services (GPS) must be turned ON for Bluetooth scanning to detect nearby classes. Please enable Location in your settings.")
                    .setPositiveButton("Enable Location") { _, _ ->
                        activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return false
            }
        }
        return true
    }

    /**
     * Helpful dialog for OEM Android skins (Xiaomi, Vivo, Oppo) that may have broken BLUETOOTH_SCAN dialogs.
     */
    fun showOemScanPermissionMessage(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Bluetooth Scan Permission Issue")
            .setMessage("Bluetooth Scan permission is required for Student mode.\n\nNote: Some device skins (e.g. certain Xiaomi, Vivo, Oppo builds) have a known system issue with the Bluetooth Scan permission dialog.\n\nTip: You can use this device as Teacher (advertiser) instead of Student (scanner), since advertising does not require this permission.")
            .setPositiveButton("Use as Teacher") { _, _ ->
                val intent = Intent(activity, com.smartroll.TeacherActivity::class.java)
                activity.startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
