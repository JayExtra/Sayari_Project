package com.dev.james.sayariproject.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.james.sayariproject.models.discover.ActiveMissions
import com.dev.james.sayariproject.models.events.ScheduledEventAlert
import com.dev.james.sayariproject.models.favourites.Result
import com.dev.james.sayariproject.models.launch.LaunchManifestItem
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMissions(mission : ActiveMissions)

    @Query("SELECT * FROM missions_table WHERE category LIKE '%' || :category || '%'")
    fun getMissionsByCategory(category: String) : Flow<List<ActiveMissions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavouriteAgency(agency : Result)

    @Query("SELECT * FROM favourite_agencies ")
    fun getFavouriteAgencies() : Flow<List<Result>>

    @Query("SELECT * FROM favourite_agencies")
    suspend fun favouriteAgenciesSnapshot(): List<Result>

    @Query("DELETE FROM favourite_agencies WHERE id = :id ")
    suspend fun deleteFavAgency(id : Int)


    //add launch into database for notification scheduling
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLaunch(launch : List<LaunchManifestItem>)

    @Query("DELETE FROM launch_manifest_table WHERE id = :id ")
    suspend fun deleteLaunchItem(id: String)

    //get Launch list from db
    @Query("SELECT * FROM launch_manifest_table")
    suspend fun getLaunchManifest() : List<LaunchManifestItem>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEvent( scheduledEventAlert: ScheduledEventAlert )

    @Query("SELECT * FROM scheduled_events_alerts")
    suspend fun getEvents() : List<ScheduledEventAlert>


    @Query("DELETE FROM scheduled_events_alerts WHERE id = :id ")
    suspend fun deleteEvent(id : Int)

    @Query("SELECT * FROM scheduled_events_alerts WHERE id = :id")
    suspend fun getEvent(id : Int) : ScheduledEventAlert






}