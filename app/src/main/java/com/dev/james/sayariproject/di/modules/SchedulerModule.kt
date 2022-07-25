package com.dev.james.sayariproject.di.modules

import android.content.Context
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.local.room.Dao
import com.dev.james.sayariproject.data.work_manager.launch_scheduler.BaseLaunchScheduler
import com.dev.james.sayariproject.data.work_manager.launch_scheduler.LaunchAlertScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SchedulerModule {
    @Provides
    @Singleton
    fun provideLaunchScheduler(
        dao : Dao ,
        @ApplicationContext context: Context ,
        dataStoreManager: DataStoreManager
    ) : BaseLaunchScheduler {
        return LaunchAlertScheduler(
            dao ,
            context ,
            dataStoreManager
        )
    }
}