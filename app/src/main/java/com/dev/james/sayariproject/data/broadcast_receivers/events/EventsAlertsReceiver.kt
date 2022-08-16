package com.dev.james.sayariproject.data.broadcast_receivers.events

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.utilities.EVENT_NOTIFICATION_ID
import com.dev.james.sayariproject.utilities.NOTIFICATION_CHANNEL_ID
import java.util.*

class EventsAlertsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val launchId  = intent?.getStringExtra("event_id")
        val name = intent?.getStringExtra("event_name")
        val slug = intent?.getStringExtra("event_slug")
        val time = Calendar.getInstance().time.toString()

        showNotification(context , name , slug)
    }

    private fun showNotification(context: Context?,name: String?, slug: String?) {
        if (context != null) {
            val notBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.rocket)
                .setContentText("$slug launch window is now open!")
                .setContentTitle(name)
                .build()

            with(NotificationManagerCompat.from(context)){
                notify(EVENT_NOTIFICATION_ID , notBuilder)
            }
        }
    }
}