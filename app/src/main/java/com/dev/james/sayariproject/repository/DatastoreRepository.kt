package com.dev.james.sayariproject.repository

import com.dev.james.sayariproject.data.local.datastore.AllPreferences
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.HAS_FINISHED_ON_BOARDING
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.HAS_SHOWN_API_MESSAGE
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.HAS_SHOWN_WELCOME_DIALOG
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_DARK_MODE_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_FIFTEEN_MIN_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_FIVE_MIN_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_FROM_FAVOURITE_AGENCIES_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_THIRTY_MIN_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.SHOULD_SHOW_NOTIFICATIONS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DatastoreRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager
 ) : BaseDataStoreRepository  {

    //will return true or false depending on whether the user did or
    //did not traverse through the on boarding screens
    override fun getOnBoardingStatus() : Flow<Boolean>{
        return dataStoreManager.readBooleanValue(HAS_FINISHED_ON_BOARDING)
    }
    //will set the on boarding status depending on whether or not the
    //user has finished on boarding
    override suspend fun setOnBoardingStatus(value : Boolean) {
        dataStoreManager.storeBooleanValue(HAS_FINISHED_ON_BOARDING , value)
    }



    override suspend fun setHasShownWelcomeMessage(value : Boolean){
        dataStoreManager.storeBooleanValue(HAS_SHOWN_WELCOME_DIALOG , value)
    }

    override suspend fun setHasShownApiMessage(value : Boolean){
        dataStoreManager.storeBooleanValue(HAS_SHOWN_API_MESSAGE , value)
    }

    override fun readHasShownApiMessage() : Flow<Boolean>{
        return dataStoreManager.readBooleanValueAsFlow(HAS_SHOWN_API_MESSAGE)
    }

    override fun readHasShownWelcomeMessage() : Flow<Boolean>{
        return dataStoreManager.readBooleanValueAsFlow(HAS_SHOWN_WELCOME_DIALOG)
    }

    override suspend fun saveNotificationStatus(value: Boolean) {
        dataStoreManager.storeBooleanValue(SHOULD_SHOW_NOTIFICATIONS , value)
    }

    override suspend fun readNotificationStatusOnce(): Boolean {
       return dataStoreManager.readBooleanValueOnce(SHOULD_SHOW_NOTIFICATIONS)
    }

    //will set the status of light or dark mode of the application
    override suspend fun setDarLightModeStatus(value : Boolean){
        dataStoreManager.storeBooleanValue(IS_DARK_MODE_ENABLED , value)
    }

    //for favourite launches. Will show notifications for only favourite launches
    override suspend fun setFavouriteAgenciesStatus(value : Boolean){
        dataStoreManager.storeBooleanValue(IS_FROM_FAVOURITE_AGENCIES_ENABLED , value)
    }

    //will set notification thirty minutes before event/launch
    override suspend fun setThrityMinIntervalStatus(value : Boolean){
        dataStoreManager.storeBooleanValue(IS_THIRTY_MIN_ENABLED ,value)
    }

    //will set notification fifteen minutes before event/launch
    override suspend fun setFifteenMinIntervalStatus(value : Boolean){
        dataStoreManager.storeBooleanValue(IS_FIFTEEN_MIN_ENABLED , value)
    }

    //will set notification five minutes before event/launch
    override suspend fun setFiveMinIntervalStatus(value : Boolean){
        dataStoreManager.storeBooleanValue(IS_FIVE_MIN_ENABLED , value)
    }

    //returns all settings saved values
    override fun getAllSettingsPreferences() : Flow<AllPreferences>{
        return dataStoreManager.settingsPreferencesFlow
    }

}

