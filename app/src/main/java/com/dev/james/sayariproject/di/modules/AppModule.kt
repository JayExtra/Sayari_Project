package com.dev.james.sayariproject.di.modules

import android.content.Context
import com.dev.james.sayariproject.data.datasources.ArticlesDataSource
import com.dev.james.sayariproject.data.datasources.SpaceFlightApiDataSource
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.repository.MainRepository
import com.dev.james.sayariproject.utilities.ARTICLE_BASE_URL
import com.dev.james.sayariproject.utilities.LAUNCH_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //setup okhttp interceptor + client
    private val loggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    //article client
    private val articleClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    //launch client
    private val launchClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()


    //TELL HILT HOW TO PROVIDE ARTICLE RETROFIT INSTANCE
    @ArticleRetrofitResponse
    @Provides
    @Singleton
    fun provideArticleRetrofit() : Retrofit =
        Retrofit.Builder()
            .baseUrl(ARTICLE_BASE_URL)
            .client(articleClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    //TELL HILT HOW TO PROVIDE LAUNCH RETROFIT INSTANCE
    @LaunchRetrofitResponse
    @Provides
    @Singleton
    fun provideLaunchRetrofit() : Retrofit =
        Retrofit.Builder()
            .baseUrl(LAUNCH_BASE_URL)
            .client(launchClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    //PROVIDE ARTICLES API
    @Provides
    @Singleton
    fun provideArticleApi(
        @ArticleRetrofitResponse retrofit : Retrofit
    ) : NewsApiService =
        retrofit.create(NewsApiService::class.java)

    //provide datastore
    @Provides
    @Singleton
    fun provideDatastore(
        @ApplicationContext appContext : Context
    ) : DataStoreManager {
        return DataStoreManager(appContext)
    }

    @Provides
    @Singleton
    fun provideRepository(
        articlesDataSource: ArticlesDataSource
    ) : BaseMainRepository {
        return MainRepository(articlesDataSource)
    }

    //provide articles datasource
    @Provides
    @Singleton
    fun provideSpaceFlightApiDataSource(api : NewsApiService) : SpaceFlightApiDataSource {
        return ArticlesDataSource(api)
    }

}
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArticleRetrofitResponse

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LaunchRetrofitResponse

