package cz.tomashavlicek.mildybackgroundlocation.permissions

import android.Manifest.permission.*
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED

/**
 * Simple helper for permissions.
 */
class PermissionHelper {

    fun hasLocationPermission(context: Context, withBackgroundLocation: Boolean) : Boolean {
        return if(withBackgroundLocation) hasBackgroundLocationPermission(context)
        else hasLocationPermission(context)
    }

    private fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasLocationPermission(context) && isPermissionGranted(context, ACCESS_BACKGROUND_LOCATION)
        } else {
            hasLocationPermission(context)
        }
    }

    private fun hasLocationPermission(context: Context) : Boolean {
        return isPermissionGranted(context, ACCESS_COARSE_LOCATION) && isPermissionGranted(context, ACCESS_FINE_LOCATION)
    }

    private fun isPermissionGranted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
}