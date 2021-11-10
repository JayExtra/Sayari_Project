package com.dev.james.sayariproject.di

import android.content.Context
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //provide datastore
    @Provides
    @Singleton
    fun provideDatastore(
        @ApplicationContext appContext : Context
    ) : DataStoreManager {
        return DataStoreManager(appContext)
    }

}