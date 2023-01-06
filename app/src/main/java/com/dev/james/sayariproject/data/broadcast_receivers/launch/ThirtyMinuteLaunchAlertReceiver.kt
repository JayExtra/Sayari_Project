package com.dev.james.sayariproject.data.broadcast_receivers.launch

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.utilities.FIVE_MIN_LAUNCH_NOTIFICATION_ID
import com.dev.james.sayariproject.utilities.SAYARI_MAIN_NOTIFICATION_CHANNEL
import com.dev.james.sayariproject.utilities.THIRTY_MIN_LAUNCH_NOTIFICATION_ID
import com.dev.james.sayariproject.utilities.THIRTY_MIN_NOTIFICATION_CHANNEL
import java.util.*

class ThirtyMinuteLaunchAlertReceiver : BroadcastReceiver() {

    private lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context?, intent: Intent?) {
        //on receive i.e when the alarm hits at the required time a
        //notification will be shown then the notification registered in the db
        val launchId  = intent?.getStringExtra("launch_id")
        val name = intent?.getStringExtra("launch_name")
        val slug = intent?.getStringExtra("launch_slug")
        val time = Calendar.getInstance().time.toString()

        notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        showNotification(context = context , name =  name , slug =  slug)
    }

    private fun showNotification(context: Context?,name: String?, slug: String?) {
        if (context != null) {
            val notBuilder = NotificationCompat.Builder(context, THIRTY_MIN_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText("$slug launch window will open in 30 minutes")
                .setContentTitle(name)
                .build()

            notificationManager.notify(
                THIRTY_MIN_LAUNCH_NOTIFICATION_ID , notBuilder
            )

        }
    }
}