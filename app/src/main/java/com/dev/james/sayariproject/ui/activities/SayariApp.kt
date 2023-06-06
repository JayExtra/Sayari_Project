package com.dev.james.sayariproject.ui.activities

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dev.james.sayariproject.utilities.*
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SayariApp : Application()  , Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        setupTimber()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val mainChannel = NotificationChannel(
                SAYARI_MAIN_NOTIFICATION_CHANNEL ,
                SAYARI_NOTIFICATION_CHANNEL_DESCRIPTION,
                NotificationManager.IMPORTANCE_HIGH
            )
            val thirtyMinChannel = NotificationChannel(
                THIRTY_MIN_NOTIFICATION_CHANNEL ,
                THIRTY_MIN_NOTIFICATION_CHANNEL_DESCRIPTION ,
                NotificationManager.IMPORTANCE_HIGH
            )
            val fifteenMinChannel = NotificationChannel(
                FIFTEEN_MIN_NOTIFICATION_CHANNEL ,
                FIFTEEN_MIN_NOTIFICATION_CHANNEL_DESCRIPTION ,
                NotificationManager.IMPORTANCE_HIGH
            )
            val fiveMinChannel = NotificationChannel(
                FIVE_MIN_NOTIFICATION_CHANNEL ,
                FIVE_MIN_NOTIFICATION_CHANNEL_DESCRIPTION ,
                NotificationManager.IMPORTANCE_HIGH
            )

            mainChannel.description = SAYARI_NOTIFICATION_CHANNEL_DESCRIPTION
            thirtyMinChannel.description = THIRTY_MIN_NOTIFICATION_CHANNEL_DESCRIPTION
            fifteenMinChannel.description = FIFTEEN_MIN_NOTIFICATION_CHANNEL_DESCRIPTION
            fiveMinChannel.description =  FIVE_MIN_NOTIFICATION_CHANNEL_DESCRIPTION

            val channelList = mutableListOf<NotificationChannel>()
            channelList.add(mainChannel)
            channelList.add(thirtyMinChannel)
            channelList.add(fifteenMinChannel)
            channelList.add(fiveMinChannel)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE ) as NotificationManager
            notificationManager.createNotificationChannels(channelList)
        }
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun setupTimber(){
        Timber.plant(Timber.DebugTree())
    }

}