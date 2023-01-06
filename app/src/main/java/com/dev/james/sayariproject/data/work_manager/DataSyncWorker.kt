package com.dev.james.sayariproject.data.work_manager

import android.content.Context
import android.util.Log
import androidx.datastore.dataStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys
import com.dev.james.sayariproject.data.local.room.Dao
import com.dev.james.sayariproject.data.remote.service.LaunchApiService
import com.dev.james.sayariproject.data.work_manager.WorkerKeys.ERROR_MESSAGE
import com.dev.james.sayariproject.data.work_manager.WorkerKeys.SUCCESS_MESSAGE
import com.dev.james.sayariproject.models.launch.LaunchManifestItem
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted private val context : Context,
    @Assisted private val parameters : WorkerParameters,
    private val api : LaunchApiService ,
    private val dao: Dao
) : CoroutineWorker(context , parameters) {
    override suspend fun doWork(): Result {

        val apiResponse = api.getUpcomingLaunchesForSync(null , 50 , 0)

            apiResponse.body()?.let { body ->

                val launchManifestList = body.launchList.map {
                    LaunchManifestItem(
                        id = it.id ,
                        slug = it.slug,
                        name = it.name,
                        window_start = it.startWindow
                    )
                }

                    try {
                        dao.addLaunch(launchManifestList)
                        return Result.success(
                            workDataOf(SUCCESS_MESSAGE to "sync performed successfully")
                        )

                    }catch (e : Exception){
                        return Result.failure(
                            workDataOf(
                                ERROR_MESSAGE to e.localizedMessage
                            )
                        )
                    }


            }

        if(!apiResponse.isSuccessful){
            Log.d("DataSyncWorker", "doWork: error! failed network call  ")
            if(apiResponse.code().toString().startsWith("5")){

                Log.d("DataSyncWorker", "doWork: error! server error  ")
                return Result.retry()
            }
            return Result.retry()
        }

        return Result.failure(
            workDataOf(ERROR_MESSAGE to "Unknown error occurred")
        )
    }
}