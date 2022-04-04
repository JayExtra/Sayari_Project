package com.dev.james.sayariproject.di.modules

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.dev.james.sayariproject.BuildConfig
import com.dev.james.sayariproject.data.datasources.discover.BaseDiscoverFragmentDatasource
import com.dev.james.sayariproject.data.datasources.discover.DiscoverFragmentDatasource
import com.dev.james.sayariproject.data.datasources.events.BaseEventsDatasource
import com.dev.james.sayariproject.data.datasources.events.EventsDatasource
import com.dev.james.sayariproject.data.datasources.favourites.BaseFavouritesDataSource
import com.dev.james.sayariproject.data.datasources.favourites.FavouritesDataSource
import com.dev.james.sayariproject.data.remote.service.EventsApiService
import com.dev.james.sayariproject.data.datasources.home.ArticlesDataSource
import com.dev.james.sayariproject.data.datasources.home.BaseTopArticlesDataSource
import com.dev.james.sayariproject.data.datasources.home.SpaceFlightApiDataSource
import com.dev.james.sayariproject.data.datasources.home.TopArticlesDataSource
import com.dev.james.sayariproject.data.datasources.iss.BaseIssDataSource
import com.dev.james.sayariproject.data.datasources.iss.IssDataSource
import com.dev.james.sayariproject.data.datasources.launches.LaunchesBaseDatasource
import com.dev.james.sayariproject.data.datasources.launches.LaunchesDataSource
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.local.room.Dao
import com.dev.james.sayariproject.data.local.room.SayariDatabase
import com.dev.james.sayariproject.data.remote.service.LaunchApiService
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //setup okhttp interceptor + client
    private val loggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)


    @ArticlesOkhttpClient
    @Provides
    @Singleton
    fun providesArticleOkHttpClient(cache: Cache): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(cacheInterceptor)
            .cache(cache)
        if (BuildConfig.DEBUG) okHttpClient.addInterceptor(loggingInterceptor)

        return okHttpClient.build()
    }

    @LaunchesOkhttpClient
    @Singleton
    @Provides
    fun provideLaunchesOkHttpClient(cache: Cache):OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(cacheInterceptor)
            .cache(cache)
        if (BuildConfig.DEBUG) okHttpClient.addInterceptor(loggingInterceptor)

        return okHttpClient.build()
    }

    //Events okhttp client
    @EventsOkhttpClient
    @Singleton
    @Provides
    fun provideEventsOkhttpClient(cache :Cache):OkHttpClient{
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(cacheInterceptor)
            .cache(cache)
        if (BuildConfig.DEBUG) okHttpClient.addInterceptor(loggingInterceptor)
        return okHttpClient.build()

    }


    //TELL HILT HOW TO PROVIDE ARTICLE RETROFIT INSTANCE
    @ArticleRetrofitResponse
    @Provides
    @Singleton
    fun provideArticleRetrofit(
        @ArticlesOkhttpClient okHttpClient: OkHttpClient
    ) : Retrofit =
        Retrofit.Builder()
            .baseUrl(ARTICLE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()



    //TELL HILT HOW TO PROVIDE LAUNCH RETROFIT INSTANCE
    @LaunchRetrofitResponse
    @Provides
    @Singleton
    fun provideLaunchRetrofit(
       @LaunchesOkhttpClient okHttpClient: OkHttpClient
    ) : Retrofit =
        Retrofit.Builder()
            .baseUrl(LAUNCH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    //TELL HILT OW TO PROVIDE EVENTS RETROFIT INSTANCE
    @EventsRetrofitResponse
    @Provides
    @Singleton
    fun provideEventsRetrofit(
        @EventsOkhttpClient okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(LAUNCH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    //PROVIDE ARTICLES API
    @Provides
    @Singleton
    fun provideArticleApi(
        @ArticleRetrofitResponse retrofit : Retrofit
    ) : NewsApiService =
        retrofit.create(NewsApiService::class.java)

    //provide launches api
    @Provides
    @Singleton
    fun provideLaunchesApi(
        @LaunchRetrofitResponse retrofit: Retrofit
    ):LaunchApiService = retrofit.create(LaunchApiService::class.java)

    //provide events api
    @Provides
    @Singleton
    fun provideEventsApi(
        @EventsRetrofitResponse retrofit : Retrofit
    ) : EventsApiService = retrofit.create(EventsApiService::class.java)

    //provide datastore
    @Provides
    @Singleton
    fun provideDatastore(
        @ApplicationContext appContext : Context
    ) : DataStoreManager {
        return DataStoreManager(appContext)
    }

    //provide database

    @Provides
    @Singleton
    fun provideDatabase(app: Application , callback : SayariDatabase.Callback) =
        Room.databaseBuilder(app , SayariDatabase::class.java, "sayari_database")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Singleton
    @Provides
    fun provideDbDao(db : SayariDatabase) =
        db.dbDao()


    @Provides
    @Singleton
    fun provideRepository(
        articlesDataSource: SpaceFlightApiDataSource,
        topArticlesDataSource: BaseTopArticlesDataSource,
        launchesDatasource: LaunchesBaseDatasource,
        discoverFragmentDatasource: BaseDiscoverFragmentDatasource,
        eventsDatasource: BaseEventsDatasource,
        issDataSource: BaseIssDataSource,
        favouritesDataSource: BaseFavouritesDataSource
    ) : BaseMainRepository {
        return MainRepository(articlesDataSource ,
            topArticlesDataSource,
            launchesDatasource ,
            discoverFragmentDatasource,
            eventsDatasource,
            issDataSource,
            favouritesDataSource
        )
    }

    //------------------------------provide all datasource----------------------------------------------------
    @Provides
    @Singleton
    fun provideSpaceFlightApiDataSource(api : NewsApiService) : SpaceFlightApiDataSource {
        return ArticlesDataSource(api)
    }

    @Provides
    @Singleton
    fun provideTopNewsDataSource(api : NewsApiService) : BaseTopArticlesDataSource {
        return TopArticlesDataSource(api)
    }

    @Provides
    @Singleton
    fun provideLaunchDataSource(api : LaunchApiService) : LaunchesBaseDatasource {
        return LaunchesDataSource(api)
    }

    @Provides
    @Singleton
    fun provideDiscoverFragDatasource(api: NewsApiService , dao : Dao) : BaseDiscoverFragmentDatasource {
        return DiscoverFragmentDatasource(api , dao)
    }

    @Provides
    @Singleton
    fun provideEventsDatasource(api : EventsApiService) : BaseEventsDatasource {
        return EventsDatasource(api)
    }

    @Provides
    @Singleton
    fun provideIssDataSource(
        api : LaunchApiService,
        eventsApi : EventsApiService
    ) : BaseIssDataSource {
        return IssDataSource(api , eventsApi)
    }

    @Provides
    @Singleton
    fun provideFavouritesDataSource(
        api : LaunchApiService,
        dao: Dao
    ) : BaseFavouritesDataSource {
        return FavouritesDataSource(dao , api)
    }

    /*Caching data */
    @Provides
    @Singleton
    fun provideCache(@ApplicationContext appContext: Context): Cache {

        return Cache(
            File(appContext.applicationContext.cacheDir, "articles_cache"),
            10 * 1024 * 1024
        )
    }


    private val cacheInterceptor = object : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val response: Response = chain.proceed(chain.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(30, TimeUnit.DAYS)
                .build()
            return response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }
    }

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class  ApplicationScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArticleRetrofitResponse

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LaunchRetrofitResponse

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EventsRetrofitResponse

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArticlesOkhttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LaunchesOkhttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EventsOkhttpClient



