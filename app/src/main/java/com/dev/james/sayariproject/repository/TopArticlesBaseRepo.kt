package com.dev.james.sayariproject.repository

import android.util.Log
import com.dev.james.sayariproject.utilities.NetworkResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

open class TopArticlesBaseRepo {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResource<T> {
        return withContext(Dispatchers.IO) {
            try {
                NetworkResource.Success(apiCall.invoke())
            } catch (exception: Throwable) {
                Log.d("ExceptionHere", exception.toString())
                when (exception) {
                    is HttpException -> {
                        NetworkResource.Failure(
                            exception.code(),
                            exception.response()?.errorBody()
                        )
                    }
                    else -> {
                        NetworkResource.Failure(null, null)
                    }
                }
            }
        }
    }
}