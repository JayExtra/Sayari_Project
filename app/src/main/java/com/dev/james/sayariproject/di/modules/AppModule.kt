package com.dev.james.sayariproject.di.modules

import android.content.Context
import com.dev.james.sayariproject.BuildConfig
import com.dev.james.sayariproject.data.datasources.ArticlesDataSource
import com.dev.james.sayariproject.data.datasources.BaseTopArticlesDataSource
import com.dev.james.sayariproject.data.datasources.SpaceFlightApiDataSource
import com.dev.james.sayariproject.data.datasources.TopArticlesDataSource
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.remote.service.NewsApiService
import com.dev.james.sayariproject.repository.BaseMainRepository
import com.dev.james.sayariproject.repository.MainRepository
import com.dev.james.sayariproject.repository.TopArticlesBaseRepo
import com.dev.james.sayariproject.utilities.ARTICLE_BASE_URL
import com.dev.james.sayariproject.utilities.LAUNCH_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

    //launch client
    private val launchClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()


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
        articlesDataSource: SpaceFlightApiDataSource,
        topArticlesDataSource: BaseTopArticlesDataSource
    ) : BaseMainRepository {
        return MainRepository(articlesDataSource , topArticlesDataSource)
    }

    //provide articles datasource
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

}
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArticleRetrofitResponse

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LaunchRetrofitResponse

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArticlesOkhttpClient

