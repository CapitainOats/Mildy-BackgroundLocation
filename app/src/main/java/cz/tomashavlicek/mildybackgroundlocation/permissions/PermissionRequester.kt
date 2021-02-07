package cz.tomashavlicek.mildybackgroundlocation.permissions

import android.Manifest.permission.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

/**
 * Class to request and handle runtime permissions.
 */
class PermissionRequester(private val permissionsCallback: PermissionCallback) {

    fun requestLocationPermission(activity: AppCompatActivity, withBackgroundLocation: Boolean, requestCode: Int) {
        val permissions = if (!withBackgroundLocation || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        else arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION)
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    fun onRequestPermissionsResult(activity: AppCompatActivity, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResultInner(requestCode, permissions, grantResults) { permission -> wasSinglePermissionDeniedForever(activity, permission) }
    }

    private fun onRequestPermissionsResultInner(requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
                                                checkIsDeniedForever: (permission: String) -> Boolean) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.isNotEmpty()) {

            // Filters denied permissions.
            val deniedPermissions = grantResults
                    .zip(permissions)
                    .asSequence()
                    .filter { pair -> pair.first == PERMISSION_DENIED }
                    .toList()

            if (deniedPermissions.isEmpty()) {
                permissionsCallback.onPermissionsGranted(requestCode)
            } else {

                // Checks if any permission was denied with "Don't ask again option")
                if (deniedPermissions.any { pair -> checkIsDeniedForever(pair.second) }) {
                    permissionsCallback.onPermissionsDeniedForever(requestCode)
                } else {
                    permissionsCallback.onPermissionsDenied(requestCode)
                }
            }
        } else {
            permissionsCallback.onPermissionsDenied(requestCode)
        }
        return
    }

    /**
     * If a method [Fragment.shouldShowRequestPermissionRationale] returns false after user denies
     * this permission, user selected "Don't ask again option" and denied the permission.
     */
    private fun wasSinglePermissionDeniedForever(activity: AppCompatActivity, permission: String): Boolean {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
}