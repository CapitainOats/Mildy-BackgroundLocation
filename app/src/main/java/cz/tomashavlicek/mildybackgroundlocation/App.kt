package cz.tomashavlicek.mildybackgroundlocation

import android.app.Application
import android.content.Context


/**
 * Created by filip.fyrbach on 06.02.2021.
 */
class App: Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

}