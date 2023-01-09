package com.dev.james.sayariproject.data.broadcast_receivers.events

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.utilities.EVENT_NOTIFICATION_ID
import com.dev.james.sayariproject.utilities.SAYARI_MAIN_NOTIFICATION_CHANNEL
import java.util.*

class EventsAlertsReceiver : BroadcastReceiver() {

    private lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context?, intent: Intent?) {
        val launchId  = intent?.getStringExtra("event_id")
        val name = intent?.getStringExtra("event_name")
        val slug = intent?.getStringExtra("event_slug")
        val time = Calendar.getInstance().time.toString()

        notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        showNotification(context , name , slug)
    }

    private fun showNotification(context: Context?,name: String?, slug: String?) {
        if (context != null) {
            val notBuilder = NotificationCompat.Builder(context, SAYARI_MAIN_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.rocket)
                .setContentText("$slug launch window is now open!")
                .setContentTitle(name)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(
                EVENT_NOTIFICATION_ID , notBuilder
            )

        }
    }
}