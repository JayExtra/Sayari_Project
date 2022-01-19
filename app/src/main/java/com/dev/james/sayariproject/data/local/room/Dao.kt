package com.dev.james.sayariproject.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dev.james.sayariproject.models.discover.ActiveMissions
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMissions(mission : ActiveMissions)

    @Query("SELECT * FROM missions_table WHERE category LIKE '%' || :category || '%'")
    fun getMissionsByCategory(category: String) : Flow<List<ActiveMissions>>

}