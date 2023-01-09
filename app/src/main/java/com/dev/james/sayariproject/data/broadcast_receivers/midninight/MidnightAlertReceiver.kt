package com.dev.james.sayariproject.data.broadcast_receivers.midninight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dev.james.sayariproject.data.work_manager.launch_scheduler.BaseLaunchScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MidnightAlertReceiver : BroadcastReceiver() {
    @Inject
    lateinit var launchAlertScheduler: BaseLaunchScheduler
    override fun onReceive(p0: Context?, p1: Intent?) {
       CoroutineScope(context = Dispatchers.IO).launch{
           Log.d("MidnightReceiver", "inside scheduler coroutine scope")
           launchAlertScheduler.initScheduler()
       }
        Log.d("MidnightReceiver", "onReceive midnight alarm: hey I've been triggered!")
    }

}