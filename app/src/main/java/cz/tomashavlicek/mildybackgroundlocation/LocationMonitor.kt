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

private const val KEY_ACTION = "action"
private const val ACTION_REGISTER = "register"
private const val ACTION_UNREGISTER = "unregister"

/**
 * 1. Schedule periodic location updates on specific time in the future
 * 2. Receive periodic location updates.
 *
 * Created by filip.fyrbach on 06.02.2021.
 */
class LocationMonitor(
    appContext: Context,
    workerParams: WorkerParameters
)
    : Worker(appContext, workerParams) {

    private val mFusedLocationProvider: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)
    private val permissionHelper: PermissionHelper = PermissionHelper()

    companion object {

        fun register(context: Context) {
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

            val inputData = Data.Builder()
                .putString(KEY_ACTION, ACTION_REGISTER)
                .build()

            val work = OneTimeWorkRequest.Builder(LocationMonitor::class.java)
                .setInitialDelay(getInitialDelay(), TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, work)
        }

        fun unregister(context: Context) {
            val inputData = Data.Builder()
                .putString(KEY_ACTION, ACTION_UNREGISTER)
                .build()

            val work = OneTimeWorkRequest.Builder(LocationMonitor::class.java)
                .setInputData(inputData)
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

        private fun getInitialDelay(hourOfDay: Int = 20): Long {
            val current: Long = System.currentTimeMillis()

            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = hourOfDay
            calendar[Calendar.MINUTE] = 30
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0

            // Nastavime na dalsi den
            if (current > calendar.timeInMillis) {
                calendar.add(Calendar.HOUR, 24)
            }

            val delay: Long = calendar.timeInMillis - current
            Log.d(TAG, calendar.toString() + "\n" + delay / 1000 / 60)

            return calendar.timeInMillis - current
        }
    }

    override fun doWork(): Result {
        val action = inputData.getString(KEY_ACTION) ?: return Result.failure()

        if (action == ACTION_REGISTER) {
            return startTrackingLocation()
        } else if (action == ACTION_UNREGISTER) {
            val locationUpdatePendingIntent = createLocationUpdatePendingIntent()
            mFusedLocationProvider.removeLocationUpdates(locationUpdatePendingIntent)
            return Result.success()
        }

        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun startTrackingLocation(): Result {
        if (!permissionHelper.hasLocationPermission(applicationContext, false)) {
            Log.d(TAG, "Not sufficient permissions for tracking location.")
            return Result.failure()
        }

        val locationUpdatePendingIntent = createLocationUpdatePendingIntent()
        mFusedLocationProvider.requestLocationUpdates(
            getLocationRequest(),
            locationUpdatePendingIntent
        )

        Log.d(TAG, "Start tracking location.")
        return Result.success()
    }

    private fun createLocationUpdatePendingIntent(): PendingIntent {
        val locationUpdateIntent = Intent(applicationContext, LocationUpdateReceiver::class.java)
        return PendingIntent.getBroadcast(
            applicationContext,
            LOCATION_UPDATE,
            locationUpdateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
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