package cz.tomashavlicek.mildybackgroundlocation

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import cz.tomashavlicek.mildybackgroundlocation.permissions.PermissionHelper
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

const val LOCATION_UPDATE = 100
const val TAG = "LocationMonitor"

/**
 * 1. Schedule periodic location updates on specific time in the futrue
 * 2. Receive periodic location updates.
 *
 * Created by filip.fyrbach on 06.02.2021.
 */
class LocationMonitor(appContext: Context,
                      workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    private val mFusedLocationProvider: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)
    private val permissionHelper: PermissionHelper = PermissionHelper()

    companion object {

        fun run(context: Context) {
            // Uz jsme scheduler spustili a uspesne jsme nastavili polsuchac na location updates
            if (isWorkSucceeded(context)) {
                Log.d(TAG, "Already successfully scheduled.")
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .build()

            val work = OneTimeWorkRequest.Builder(LocationMonitor::class.java)
                .setInitialDelay(getInitialDelay(), TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, work)
        }

        private fun isWorkSucceeded(context: Context): Boolean {
            val instance = WorkManager.getInstance(context)
            val statuses: ListenableFuture<List<WorkInfo>> = instance.getWorkInfosByTag(TAG)
            return try {
                var succeeded = false
                val workInfoList: List<WorkInfo> = statuses.get()
                for (workInfo in workInfoList) {
                    val state = workInfo.state
                    succeeded = state == WorkInfo.State.SUCCEEDED
                }
                succeeded
            } catch (e: ExecutionException) {
                e.printStackTrace()
                false
            } catch (e: InterruptedException) {
                e.printStackTrace()
                false
            }
        }

        private fun getInitialDelay(hourOfDay: Int = 19): Long {
            val current: Long = System.currentTimeMillis()

            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = hourOfDay
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0

            // Nastavime na dalsi den
            if (current > calendar.timeInMillis) {
                calendar.add(Calendar.HOUR, 24)
            }

            return calendar.timeInMillis
        }
    }

    override fun doWork(): Result {
        return startTrackingLocation()
    }

    @SuppressLint("MissingPermission")
    private fun startTrackingLocation(): Result {
        val locationUpdateIntent = Intent(applicationContext, LocationUpdateReceiver::class.java)
        val locationUpdatePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            LOCATION_UPDATE,
            locationUpdateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (!permissionHelper.hasLocationPermission(applicationContext, false)) {
            Log.d(TAG, "Not sufficient permissions for tracking location.")
            return Result.failure()
        }

        mFusedLocationProvider.requestLocationUpdates(
            getLocationRequest(),
            locationUpdatePendingIntent
        )

        Log.d(TAG, "Start tracking location.")
        return Result.success()
    }

    /**
     * Sets up the location request.
     *
     * @return The LocationRequest object containing the desired parameters.
     */
    private fun getLocationRequest() = LocationRequest().apply {
        interval = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)
        fastestInterval = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }
}