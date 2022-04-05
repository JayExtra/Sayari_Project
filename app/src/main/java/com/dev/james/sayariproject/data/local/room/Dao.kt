package com.dev.james.sayariproject.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.james.sayariproject.models.discover.ActiveMissions
import com.dev.james.sayariproject.models.favourites.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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

    @Query("DELETE FROM favourite_agencies WHERE id = :id ")
    suspend fun deleteFavAgency(id : Int)

}