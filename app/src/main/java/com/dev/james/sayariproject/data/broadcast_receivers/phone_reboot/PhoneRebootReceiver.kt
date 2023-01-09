package com.dev.james.sayariproject.data.broadcast_receivers.phone_reboot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys
import com.dev.james.sayariproject.data.work_manager.launch_scheduler.BaseLaunchScheduler
import com.dev.james.sayariproject.data.work_manager.launch_scheduler.LaunchAlertScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class PhoneRebootReceiver : BroadcastReceiver() {
   /* @Inject
    lateinit var dataStoreManager: DataStoreManager*/

    @Inject
    lateinit var launchAlertScheduler: BaseLaunchScheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("PhoneRebootRec", "onReceive: Reboot Received")
        if(intent?.action.equals("android.intent.action.BOOT_COMPLETED")){
            CoroutineScope(context = Dispatchers.IO).launch {
                launchAlertScheduler.initScheduler()
            }
           // val hasWorkerFiredOnce = runBlocking { dataStoreManager.readBooleanValueOnce(DatastorePreferenceKeys.HAS_LAUNCH_SCHEDULER_FIRED_ONCE) }
          //  if(hasWorkerFiredOnce)
          //  }
        }
    }
}