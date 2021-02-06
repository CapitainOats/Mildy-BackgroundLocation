package cz.tomashavlicek.mildybackgroundlocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private val REQUEST_LOCATION_PERMISSION = 1

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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
