package com.dev.james.sayariproject.utilities

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.net.ssl.SSLException


@RequiresApi(Build.VERSION_CODES.O)
fun String.toDateString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val date = formatter.parse(this)
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
    return dateFormatter.format(date)
}
@RequiresApi(Build.VERSION_CODES.O)
fun String.toDateStringDateOnly(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val date = formatter.parse(this)
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormatter.format(date)
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toTimeStringOnly(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val date = formatter.parse(this)
    val dateFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return dateFormatter.format(date)
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toDateObject(): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.parse(this, formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.isDateFuture(): Boolean {
    val now = LocalDate.now()
    return this.isAfter(now)
}
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.isDateNow(): Boolean {
    val now = LocalDate.now()
    return this.isEqual(now)
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.formatTimeToAMPM(): String {
    val time = LocalTime.parse(this)
    return DateTimeFormatter.ofPattern("hh:mm a")
        .withLocale(Locale.getDefault())
        .format(time)
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.isPastTime(): Boolean {
    val currentTime = LocalTime.now()
    val timeToCompare = LocalTime.parse(this, DateTimeFormatter.ofPattern("HH:mm:ss"))
    Timber.d("Extensions : $timeToCompare")
    return currentTime.isAfter(timeToCompare)
}

fun IOException.returnMessageString(): String {
    return when (this) {

        is ConnectException -> {
            "Could not establish a connection , please check your network and try again."
        }
        is SocketTimeoutException -> {
            "Socket timeout: Maximum connection retries exceeded."
        }
        is SSLException -> {
            "Could not establish a secure connection to the server."
        }
        is ProtocolException -> {
            "Unexpected error occurred. Please try again later."
        }
        else -> {
            "Hmm..something seems wrong on your end. Please check your network connection and try again later."
        }
    }
}

fun errorBodyAsString(throwable: HttpException): String? {
    val reader = throwable.response()?.errorBody()?.charStream()
    return reader?.use { it.readText() }
}

fun Int.mapResponseCodeToErrorMessage(): String = when (this) {
    HttpURLConnection.HTTP_UNAUTHORIZED -> "Your access token has expired. Please login again."
    in 400..499 -> "Hmm..something seems wrong on your end. Please check your network connection and try again later."
    in 500..600 -> "Oh , something seems wrong on our end. Please be patient as we fix things."
    else -> "Could not establish connection at the moment. Please be patient as we work through it."
}