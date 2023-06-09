package com.dev.james.sayariproject.data.local.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

object DatastorePreferenceKeys {
    val STORE_NAME = "com.dev.james.sayariproject.sayari_datastore"

    //value for on-boarding section
    val HAS_FINISHED_ON_BOARDING : Preferences.Key<Boolean> =
        booleanPreferencesKey("has_finished_on_boarding")

    val HAS_SHOWN_WELCOME_DIALOG : Preferences.Key<Boolean> =
        booleanPreferencesKey("has_shown_welcome")

    val HAS_SHOWN_API_MESSAGE : Preferences.Key<Boolean> =
        booleanPreferencesKey("has_shown_api_message")

    val SHOULD_SHOW_NOTIFICATIONS : Preferences.Key<Boolean> =
        booleanPreferencesKey("should_show_notifications")

    //value for dark and light mode
    val IS_DARK_MODE_ENABLED : Preferences.Key<Boolean> =
        booleanPreferencesKey("is_dark_mode_enabled")

    //value for notifications from favourite agencies only
    val IS_FROM_FAVOURITE_AGENCIES_ENABLED : Preferences.Key<Boolean> =
        booleanPreferencesKey("is_from_favourite_agencies_enabled")

    //show notification 30 min before launch
    val IS_THIRTY_MIN_ENABLED : Preferences.Key<Boolean> =
        booleanPreferencesKey("is_thirty_minutes_enabled")

    //show notifications 15 minutes before
    val IS_FIFTEEN_MIN_ENABLED : Preferences.Key<Boolean> =
        booleanPreferencesKey("is_fifteen_minutes_enabled")

    //show notification five 5 before
    val IS_FIVE_MIN_ENABLED : Preferences.Key<Boolean> =
        booleanPreferencesKey("is_five_minutes_enabled")

    val HAS_PERFORMED_SYNC : Preferences.Key<Boolean> =
        booleanPreferencesKey("has_performed_sync")

    val HAS_LAUNCH_SCHEDULER_FIRED_ONCE : Preferences.Key<Boolean> =
        booleanPreferencesKey("has_fired_once")


}