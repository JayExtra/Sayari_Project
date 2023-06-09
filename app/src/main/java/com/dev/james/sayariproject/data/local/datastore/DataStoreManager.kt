package com.dev.james.sayariproject.data.local.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_DARK_MODE_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_FIFTEEN_MIN_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_FIVE_MIN_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_FROM_FAVOURITE_AGENCIES_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.IS_THIRTY_MIN_ENABLED
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.SHOULD_SHOW_NOTIFICATIONS
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.STORE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class DataStoreManager @Inject constructor(
    @ApplicationContext private val context : Context
){
    companion object{
        private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = STORE_NAME)
    }

    //store any boolean value in the datastore with its desired key
    suspend fun storeBooleanValue(key : Preferences.Key<Boolean> , value : Boolean){
        context.dataStore.edit {
            it[key] = value
        }
    }

    //read  any boolean value from datastore using its key , returns a flow
    //of boolean value
    fun readBooleanValue(key : Preferences.Key<Boolean>) : Flow<Boolean> {
        return context.dataStore.data.map {
            it[key]?:false
        }.catch { exception ->
            if(exception is IOException){
                Log.e("DatastoreManager", "readBooleanValue: error reading exception", exception)
                emit(false)
            }
        }
    }

     fun readBooleanValueAsFlow(
        key: Preferences.Key<Boolean>
    ): Flow<Boolean> {
        return context.dataStore.data.map {
            it[key] ?: false
        }.catch { exception ->
            if (exception is IOException) {
                Timber.e(
                    "DataStoreManager readBooleanValueAsFlow error =>" +
                            exception.localizedMessage
                )
                emit(false)
            }
        }
    }

    suspend fun readBooleanValueOnce(key : Preferences.Key<Boolean>) =
        context.dataStore.data.first()[key] ?: false

     val settingsPreferencesFlow = context.dataStore.data.catch { exception ->
        if(exception is IOException){
            Log.e("DatastoreManager", "Error:error fetching preferences ", exception )
            emit(emptyPreferences())
        }else{
            throw exception
        }
    }.map { preferences ->
             val dayNightModeValue = preferences[IS_DARK_MODE_ENABLED] ?: false
             val favouriteAgenciesValue = preferences[IS_FROM_FAVOURITE_AGENCIES_ENABLED] ?: false
             val thirtyMinutesValue = preferences[IS_THIRTY_MIN_ENABLED] ?: true
             val fifteenMinutesValue = preferences[IS_FIFTEEN_MIN_ENABLED] ?: false
             val fiveMinutesValue = preferences[IS_FIVE_MIN_ENABLED] ?: true
            val shouldShowNotifications = preferences[SHOULD_SHOW_NOTIFICATIONS] ?: false

         AllPreferences(dayNightModeValue , favouriteAgenciesValue ,
         thirtyMinutesValue , fifteenMinutesValue , fiveMinutesValue , shouldShowNotifications)
         }


}
data class AllPreferences(
    val nightDarkStatus : Boolean,
    val favouriteAgencies : Boolean,
    val thirtyMinStatus : Boolean,
    val fifteenMinStatus : Boolean,
    val fiveMinStatus : Boolean ,
    val showNotifications : Boolean
)
