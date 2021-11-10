package com.dev.james.sayariproject.repository

import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.HAS_FINISHED_ON_BOARDING
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DatastoreRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager
 )  {

    //will return true or false depending on whether the user did or
    //did not traverse through the on boarding screens
    fun getOnBoardingStatus() : Flow<Boolean>{
        return dataStoreManager.readBooleanValue(HAS_FINISHED_ON_BOARDING)
    }

    //will set the on boarding status depending on whether or not the
    //user has finished on boarding
    suspend fun setOnBoardingStatus(value : Boolean) {
        dataStoreManager.storeBooleanValue(HAS_FINISHED_ON_BOARDING , value)
    }


}