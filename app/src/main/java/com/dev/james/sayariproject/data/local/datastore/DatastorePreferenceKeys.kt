package com.dev.james.sayariproject.data.local.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

object DatastorePreferenceKeys {
    val STORE_NAME = "com.dev.james.sayariproject.sayari_datastore"
    val HAS_FINISHED_ON_BOARDING : Preferences.Key<Boolean> =
        booleanPreferencesKey("has_finished_on_boarding")
}