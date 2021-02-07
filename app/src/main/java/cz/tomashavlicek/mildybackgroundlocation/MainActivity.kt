package cz.tomashavlicek.mildybackgroundlocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cz.tomashavlicek.mildybackgroundlocation.permissions.PermissionHelper

class MainActivity : AppCompatActivity() {

    private val REQUEST_LOCATION_PERMISSION = 1
    private val permissionHelper: PermissionHelper = PermissionHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val trackButton: Button = findViewById(R.id.trackButton)
        trackButton.setOnClickListener {
            doLocationPermissions()
        }

        // Initialize the FusedLocationClient.
        LocationMonitor.run(applicationContext)
    }

    private fun doLocationPermissions() {
        if (!permissionHelper.hasLocationPermission(this, true)) {
            val permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            else arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            ActivityCompat.requestPermissions(this,
                permissions,
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // If the permission is granted, get the location,
            // otherwise, show a Toast
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                LocationMonitor.run(applicationContext)
            } else {
                Toast.makeText(
                    this,
                    "Location Permission Denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}
