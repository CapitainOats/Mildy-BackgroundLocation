package cz.tomashavlicek.mildybackgroundlocation

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val REQUEST_LOCATION_PERMISSION = 1
    private val LOCATION_UPDATE = 100

    private lateinit var mFusedLocationProvider: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val trackButton: Button = findViewById(R.id.trackButton)
        trackButton.setOnClickListener {
            startTrackingLocation()
        }

        // Initialize the FusedLocationClient.
        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            val locationUpdateIntent = Intent(this, LocationUpdateReceiver::class.java)
            val locationUpdatePendingIntent = PendingIntent.getBroadcast(
                this,
                LOCATION_UPDATE,
                locationUpdateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            mFusedLocationProvider.requestLocationUpdates(
                getLocationRequest(),
                locationUpdatePendingIntent
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
                startTrackingLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location Permission Denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Sets up the location request.
     *
     * @return The LocationRequest object containing the desired parameters.
     */
    private fun getLocationRequest() = LocationRequest().apply {
        interval = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS)
        fastestInterval = TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}
