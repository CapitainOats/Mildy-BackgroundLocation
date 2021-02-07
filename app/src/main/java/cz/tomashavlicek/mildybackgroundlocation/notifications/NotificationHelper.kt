package cz.tomashavlicek.mildybackgroundlocation.notifications

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cz.tomashavlicek.mildybackgroundlocation.App
import cz.tomashavlicek.mildybackgroundlocation.BuildConfig
import cz.tomashavlicek.mildybackgroundlocation.R

const val packageName: String = BuildConfig.APPLICATION_ID
const val WEATHER_ALERT_NOTIFICATION_ID = 10001

/**
 * Manager starajici se o Notifikace. Singleton.
 */
object NotificationHelper {

    enum class NotificationChannelType(
        val channelId: String,
        val titleRes: Int,
        private val importance: Int,
        private val visibility: Int,
        private val showBadge: Boolean
    ) {

        CHANNEL_WEATHER_ALERT(
            "1_WeatherAlert", R.string.channel_weather_alert,
            NotificationManagerCompat.IMPORTANCE_HIGH, NotificationCompat.VISIBILITY_PUBLIC, true
        );

        @RequiresApi(api = Build.VERSION_CODES.O)
        fun createNotificationChannel(context: Context): NotificationChannel {
            val channel = NotificationChannel(
                channelId, context.getString(
                    titleRes
                ), importance
            )
            channel.lockscreenVisibility = visibility
            channel.setShowBadge(showBadge)
            return channel
        }
    }

    private val context: Context = App.context

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerToChannels(context)
        }
    }

    val breakingNewsNotificationBuilder: NotificationCompat.Builder get() {
        val builder = NotificationCompat.Builder(
            context,
            NotificationChannelType.CHANNEL_WEATHER_ALERT.channelId
        )

        builder.setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setContentTitle(context.getString(NotificationChannelType.CHANNEL_WEATHER_ALERT.titleRes))
            .setAutoCancel(true)
            .build()

        return builder
    }

    fun isNotificationChannelEnabled(context: Context, channelId: String?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!TextUtils.isEmpty(channelId)) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = manager.getNotificationChannel(channelId)
                return channel.importance != NotificationManager.IMPORTANCE_NONE
            }
            false
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun openNotificationSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            activity.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data =
                Uri.parse("package:$packageName")
            activity.startActivity(intent)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun registerToChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannelType.CHANNEL_WEATHER_ALERT.createNotificationChannel(
                context
            )
        )
    }

}