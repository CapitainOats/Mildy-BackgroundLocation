package cz.tomashavlicek.mildybackgroundlocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationResult

class LocationUpdateReceiver : BroadcastReceiver() {

    private val TAG = LocationUpdateReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        val locationResult = LocationResult.extractResult(intent)
        if (locationResult != null && !locationResult.locations.isNullOrEmpty()) {
            val location = locationResult.locations.first()
            Log.d(
                    TAG,
                    context.getString(R.string.location_text,
                            location.latitude,
                            location.longitude,
                            location.time)
            )
        } else {
            Log.d(
                    TAG,
                    "Unlucky"
            )
        }
    }
}
