package cz.tomashavlicek.mildybackgroundlocation

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cz.tomashavlicek.mildybackgroundlocation.permissions.PermissionCallback
import cz.tomashavlicek.mildybackgroundlocation.permissions.PermissionHelper
import cz.tomashavlicek.mildybackgroundlocation.permissions.PermissionRequester
import cz.tomashavlicek.mildybackgroundlocation.permissions.RequestPermissionDeniedForeverDialogFragment
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(), PermissionCallback {

    private val LOCATION_REQUEST_CODE = 14343

    private val permissionHelper: PermissionHelper = PermissionHelper()
    private lateinit var permissionRequester: PermissionRequester

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionRequester = PermissionRequester(this)

        val trackButton: Button = findViewById(R.id.trackButton)
        trackButton.setOnClickListener {
            if (!permissionHelper.hasLocationPermission(this, true)) {
                permissionRequester.requestLocationPermission(
                    this,
                    true,
                    LOCATION_REQUEST_CODE
                )
            } else {
                Toast.makeText(
                    this,
                    "All needed permissions already granted.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Initialize the FusedLocationClient.
        LocationMonitor.run(applicationContext)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            permissionRequester.onRequestPermissionsResult(
                this,
                requestCode,
                permissions,
                grantResults
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            LocationMonitor.run(this)
        }
    }

    override fun onPermissionsDenied(requestCode: Int) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            Toast.makeText(
                this,
                "Location Permission Denied.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onPermissionsDeniedForever(requestCode: Int) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            RequestPermissionDeniedForeverDialogFragment.showDialog(supportFragmentManager)
        }
    }

}
