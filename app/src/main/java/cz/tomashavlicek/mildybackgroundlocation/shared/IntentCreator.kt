package cz.tomashavlicek.mildybackgroundlocation.shared

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class IntentCreator {

    /**
     * Creates intent for application settings.
     */
    fun createAppSettingsIntent(context: Context) = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", context.packageName, null)
    }

}