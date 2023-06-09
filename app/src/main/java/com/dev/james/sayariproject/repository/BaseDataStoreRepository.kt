package com.dev.james.sayariproject.repository

import com.dev.james.sayariproject.data.local.datastore.AllPreferences
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys
import kotlinx.coroutines.flow.Flow

interface BaseDataStoreRepository {

    fun getOnBoardingStatus() : Flow<Boolean>

    suspend fun setOnBoardingStatus(value : Boolean)


    suspend fun setHasShownWelcomeMessage(value : Boolean)

    suspend fun setHasShownApiMessage(value : Boolean)

    fun readHasShownApiMessage() : Flow<Boolean>

    fun readHasShownWelcomeMessage() : Flow<Boolean>

    suspend fun saveNotificationStatus(value: Boolean)

    suspend fun readNotificationStatusOnce() : Boolean

    //will set the status of light or dark mode of the application
    suspend fun setDarLightModeStatus(value : Boolean)

    //for favourite launches. Will show notifications for only favourite launches
    suspend fun setFavouriteAgenciesStatus(value : Boolean)

    //will set notification thirty minutes before event/launch
    suspend fun setThrityMinIntervalStatus(value : Boolean)

    //will set notification fifteen minutes before event/launch
    suspend fun setFifteenMinIntervalStatus(value : Boolean)

    //will set notification five minutes before event/launch
    suspend fun setFiveMinIntervalStatus(value : Boolean)

    //returns all settings saved values
    fun getAllSettingsPreferences() : Flow<AllPreferences>


}