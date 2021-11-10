package com.dev.james.sayariproject.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys.STORE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
        }
    }


}