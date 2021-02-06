package cz.tomashavlicek.mildybackgroundlocation

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationResult
import cz.tomashavlicek.mildybackgroundlocation.notifications.NotificationManagerHelper
import cz.tomashavlicek.mildybackgroundlocation.notifications.WEATHER_ALERT_NOTIFICATION_ID

class LocationUpdateReceiver : BroadcastReceiver() {

    private val TAG = LocationUpdateReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        val locationResult = LocationResult.extractResult(intent)
        if (locationResult != null && !locationResult.locations.isNullOrEmpty()) {
            val location = locationResult.locations.first()
            val text = context.getString(
                R.string.location_text,
                location.latitude,
                location.longitude,
                location.time
            )

            Log.d(TAG, text)

            val notificationBuilder: NotificationCompat.Builder = NotificationManagerHelper.breakingNewsNotificationBuilder

            // Creating an intent for the notification
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val resultPendingIntent = PendingIntent.getActivity(
                context, WEATHER_ALERT_NOTIFICATION_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            notificationBuilder.setContentIntent(resultPendingIntent)
            notificationBuilder.setContentText(text)

            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(
                WEATHER_ALERT_NOTIFICATION_ID,
                notificationBuilder.build()
            )

        } else {
            Log.d(TAG, "Unlucky")
        }
    }
}
