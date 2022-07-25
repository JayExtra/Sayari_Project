package com.dev.james.sayariproject.data.work_manager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.HAS_LAUNCH_SCHEDULER_FIRED_ONCE
import com.dev.james.sayariproject.data.work_manager.launch_scheduler.BaseLaunchScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LaunchSchedulerWorker @AssistedInject constructor(
    @Assisted private val context : Context,
    @Assisted private val params : WorkerParameters,
    private val scheduler : BaseLaunchScheduler,
    private val dataStoreManager: DataStoreManager
) : CoroutineWorker(context , params) {
    override suspend fun doWork(): Result {

        val hasFiredOnce = dataStoreManager.readBooleanValueOnce(DatastorePreferenceKeys.HAS_LAUNCH_SCHEDULER_FIRED_ONCE)

        if(!hasFiredOnce){
            Log.d("SchedulerWorker", "doWork: has fired once => $hasFiredOnce ")
            //start scheduler
            scheduler.initScheduler()
            //update the boolean flag
            dataStoreManager.storeBooleanValue(HAS_LAUNCH_SCHEDULER_FIRED_ONCE , value = true)
            //return success
            return Result.success(
                workDataOf(
                    WorkerKeys.SUCCESS_MESSAGE to "success scheduling of launches"
                )
            )
        }else{
            Log.d("SchedulerWorker", "doWork: has fired once => $hasFiredOnce , success ")
            return Result.failure(
                workDataOf(
                    WorkerKeys.ERROR_MESSAGE to "worker has already fired once and sync performed"
                )
            )
        }

    }
}