package com.dev.james.sayariproject.notifications.firebase.messaging

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.repository.BaseDataStoreRepository
import com.dev.james.sayariproject.utilities.SAYARI_MAIN_NOTIFICATION_CHANNEL
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
@AndroidEntryPoint
class SayariFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var dataStoreRepository: BaseDataStoreRepository

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val MESSAGE_NOTIFICATION_ID = 1
        const val TAG = "SayariFirebaseMessagingService"
    }
    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("onCreate : messaging service created.")

        notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
    }

    /*handleIntent is called automatically once FCM has been received by device and the app is in the background
    * the intent carries the payload from the FCM
    * when selecting the notification card , handle intent will be used to handle the notification
    * */

    override fun handleIntent(intent: Intent?) {
        CoroutineScope(Dispatchers.Main).launch {
            val hasEnabledNotifications = dataStoreRepository.readNotificationStatusOnce()
            if(hasEnabledNotifications){
                super.handleIntent(intent)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        coroutineScope.launch {
            val hasEnabledNotifications = dataStoreRepository.readNotificationStatusOnce()
            if(hasEnabledNotifications){
                val title = message.notification?.title.toString()
                val body = message.notification?.body.toString()

                val notification = NotificationCompat.Builder(
                    applicationContext ,  SAYARI_MAIN_NOTIFICATION_CHANNEL
                ).setSmallIcon(
                    R.drawable.sayari_logo2
                ).setContentTitle(
                    title
                ).setContentText(body)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(
                    MESSAGE_NOTIFICATION_ID ,
                    notification
                )
            }
        }
    }
}