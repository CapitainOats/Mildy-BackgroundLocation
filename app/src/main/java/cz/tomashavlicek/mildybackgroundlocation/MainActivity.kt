package cz.tomashavlicek.mildybackgroundlocation

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import cz.tomashavlicek.mildybackgroundlocation.permissions.PermissionCallback
import cz.tomashavlicek.mildybackgroundlocation.permissions.PermissionHelper
import cz.tomashavlicek.mildybackgroundlocation.permissions.PermissionRequester
import cz.tomashavlicek.mildybackgroundlocation.permissions.RequestPermissionDeniedForeverDialogFragment

class MainActivity : AppCompatActivity(), PermissionCallback {

    private val LOCATION_REQUEST_CODE = 14343

    private val permissionHelper: PermissionHelper = PermissionHelper()
    private lateinit var permissionRequester: PermissionRequester

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionRequester = PermissionRequester(this)

        val notificationSwitch: SwitchCompat = findViewById(R.id.notification_switch)
        notificationSwitch.isChecked = permissionHelper.hasLocationPermission(this, true)

        val notificationBtn: LinearLayout = findViewById(R.id.notification_switch_container)
        notificationBtn.setOnClickListener {
            val isChecked = !notificationSwitch.isChecked
            notificationSwitch.isChecked = isChecked
            if (isChecked) {
                doLocationPermission()
            } else {
                LocationMonitor.unregister(this)
            }
        }

        // Initialize the FusedLocationClient.
        LocationMonitor.register(applicationContext)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
            LocationMonitor.register(this)
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

    private fun doLocationPermission() {
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

}
