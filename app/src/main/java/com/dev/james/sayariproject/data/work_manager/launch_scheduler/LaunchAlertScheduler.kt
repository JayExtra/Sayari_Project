package com.dev.james.sayariproject.data.work_manager.launch_scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.os.ConfigurationCompat
import com.dev.james.sayariproject.data.broadcast_receivers.launch.FifteenMinuteLaunchAlertReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.launch.FiveMinuteLaunchAlertReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.launch.LaunchWindowOpenAlertReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.launch.ThirtyMinuteLaunchAlertReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.midninight.MidnightAlertReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.phone_reboot.PhoneRebootReceiver
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys
import com.dev.james.sayariproject.data.local.room.Dao
import com.dev.james.sayariproject.models.launch.LaunchManifestItem
import com.dev.james.sayariproject.utilities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class LaunchAlertScheduler @Inject constructor(
    private val dao : Dao,
    @ApplicationContext private val context : Context,
    private val dataStoreManager: DataStoreManager
    ) : BaseLaunchScheduler {

    private val currentDate : Date = Calendar.getInstance().time

    override suspend fun initScheduler() {
        //1. fetches the launch manifest list from the database and returns launches
        //happening today

        val launchManifest = dao.getLaunchManifest().filter { launchItem ->
            launchItem.window_start.formatDateFromApi(context) == currentDate.formatCurrentDate(context)
        }

        Log.d("LaunchAlertScheduler", "initScheduler: available launches => $launchManifest")

        //2. schedules alarms available during that day
        scheduleUpcomingLaunches(launchManifest)

        //3. sets the midnight alert for the next day to trigger the initScheduler again
        scheduleMidnightAlarm()

        activatePhoneRebootReceiver()
    }

    private fun scheduleMidnightAlarm(){

        //Set the alarm to start at approximately 00:00 a.m
        val minutesDifference = Calendar.getInstance().get(Calendar.MINUTE) * 60000

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis() - minutesDifference
            set(Calendar.HOUR_OF_DAY , 24)
        }
        val intent = Intent(context, MidnightAlertReceiver::class.java )
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                MIDNIGHT_NOTIFICATION_ID,
                intent,
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )

        (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        val newTime = calendar.timeInMillis
        Log.d("LaunchAlertScheduler", "midnight alarm scheduled for $newTime =>  ${calendar.time} ")

    }

    //will schedule alarms for launches happening in that day
    private suspend fun scheduleUpcomingLaunches(launchManifest: List<LaunchManifestItem>) {
       if(launchManifest.isEmpty()){
           Log.d("LaunchAlertScheduler", "scheduleUpcomingLaunches: no launch found")
           checkLaunchManifest()
           return
       }

        Log.d("LaunchAlertScheduler", "scheduleUpcomingLaunches: available ${launchManifest.size} ")

        launchManifest.forEach { launch ->
           //set alarm
           setAlarm(launch.window_start , launch.name , launch.slug , launch.id)

           //update the manifest
           deleteFromManifest(launch)

       }

        //check if launch manifest is healthy i.e launch items are greater than 10
        // otherwise refresh the launch manifest
        checkLaunchManifest()

    }

    //will delete saved launch instance from db
    private suspend fun deleteFromManifest(launch: LaunchManifestItem) {
        dao.deleteLaunchItem(launch.id)
    }

    //will set various alarms respective of preferences set by the user
    // i.e. one before thirty minutes , fifteen minutes or five minutes
    private suspend fun setAlarm(windowStart: String, name: String, slug: String, id: String) {

        val thirtyMinuteSet = dataStoreManager.readBooleanValueOnce(DatastorePreferenceKeys.IS_THIRTY_MIN_ENABLED)
        val fifteenMinuteSet = dataStoreManager.readBooleanValueOnce(DatastorePreferenceKeys.IS_FIFTEEN_MIN_ENABLED)
        val fiveMinuteSet = dataStoreManager.readBooleanValueOnce(DatastorePreferenceKeys.IS_FIVE_MIN_ENABLED)

        setLaunchWindowOpenAlarm(
            windowStart = windowStart ,
            name = name ,
            id = id ,
            slug = slug)


        if(thirtyMinuteSet){
          setThirtyMinuteAlarm(
              windowStart = windowStart ,
              name = name ,
              id = id ,
              slug = slug)
        }
        if(fifteenMinuteSet){
            setFifteenMinuteAlarm(
                windowStart = windowStart ,
                name = name ,
                id = id ,
                slug = slug)
        }
        if (fiveMinuteSet){
            setFiveMinuteAlarm(
                windowStart = windowStart ,
                name = name ,
                id = id ,
                slug = slug)
        }
    }

    private fun setLaunchWindowOpenAlarm(windowStart : String, name: String, id: String, slug: String){
        val date = windowStart.toDate()?.time
        Log.d("LaunchAlertScheduler", "setAlarm launch window: ${date?.let { Date(it).formatCurrentDate(context) }}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context , LaunchWindowOpenAlertReceiver::class.java).apply {
            putExtra("launch_id" , id )
            putExtra("launch_name" , name)
            putExtra("launch_slug" , slug)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context ,
            WINDOW_OPEN_NOTIFICATION_ID ,
            intent ,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        if (date != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP , date , pendingIntent)
            Log.d("LaunchAlertScheduler", "launch alert set for: ${windowStart.toDate()}")
        }
    }



    private fun setThirtyMinuteAlarm(windowStart : String , name: String , id: String , slug: String){
        val date = windowStart.toDate()?.time?.minus(30 * 60 * 1000)
        Log.d("LaunchAlertScheduler", "setAlarm: 30 min before : ${date?.let { Date(it).formatCurrentDate(context) }}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context , ThirtyMinuteLaunchAlertReceiver::class.java).apply {
            putExtra("launch_id" , id )
            putExtra("launch_name" , name)
            putExtra("launch_slug" , slug)
        }

        val pendingIntent = PendingIntent.getBroadcast(context,
            THIRTY_MIN_LAUNCH_NOTIFICATION_ID ,
            intent ,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        if (date != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP , date , pendingIntent)
            Log.d("LaunchAlertScheduler", "launch alert set for: ${windowStart.toDate()}")
        }
    }
    private fun setFifteenMinuteAlarm(windowStart : String , name: String , id: String , slug: String){
        val date = windowStart.toDate()?.time?.minus(15 * 60 * 1000)
        Log.d("LaunchAlertScheduler", "setAlarm: 15 min before : ${date?.let { Date(it).formatCurrentDate(context) }}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context , FifteenMinuteLaunchAlertReceiver::class.java).apply {
            putExtra("launch_id" , id )
            putExtra("launch_name" , name)
            putExtra("launch_slug" , slug)
        }

        val pendingIntent = PendingIntent.getBroadcast(context ,
            FIFTEEN_MIN_LAUNCH_NOTIFICATION_ID ,
            intent ,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        if (date != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP , date , pendingIntent)
            Log.d("LaunchAlertScheduler", "launch alert set for: ${windowStart.toDate()}")
        }
    }

    private fun setFiveMinuteAlarm(windowStart : String , name: String , id: String , slug: String){
        val date = windowStart.toDate()?.time?.minus(5 * 60 * 1000)
        Log.d("LaunchAlertScheduler", "setAlarm: 15 min added : ${date?.let { Date(it).formatCurrentDate(context) }}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context , FiveMinuteLaunchAlertReceiver::class.java).apply {
            putExtra("launch_id" , id )
            putExtra("launch_name" , name)
            putExtra("launch_slug" , slug)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context ,
            FIVE_MIN_LAUNCH_NOTIFICATION_ID ,
            intent ,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        if (date != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP , date , pendingIntent)
            Log.d("LaunchAlertScheduler", "launch alert set for: ${windowStart.toDate()}")
        }
    }

    private suspend fun checkLaunchManifest(){
        val launchListSize = dao.getLaunchManifest().size
        if(launchListSize<= 10) {
            Log.d("LaunchAlertScheduler", "The manifest is not healthy. Launch list size => $launchListSize")
            dataStoreManager.storeBooleanValue(DatastorePreferenceKeys.HAS_PERFORMED_SYNC , false)
        }else {
            Log.d("LaunchAlertScheduler", "The manifest is healthy. Launch list size => $launchListSize")
        }
    }

    private fun activatePhoneRebootReceiver(){
        val receiver = ComponentName(context, PhoneRebootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    //will format the date from API into local time
    private fun String.formatDateFromApi(context : Context) : String {
        return try {
            val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("GMT")
            val passedDate: Date = inputFormat.parse(this) as Date

            //Here you put how you want your date to be, this looks like this Tue,Nov 2, 2021, 12:23 pm
            val outputFormatDay = SimpleDateFormat("dd-MM-yyyy", currentLocale)
            outputFormatDay.timeZone = TimeZone.getDefault()
            val newDateString = outputFormatDay.format(passedDate)

            newDateString

        }catch (_ : Exception){
            "00:00:00"
        }
    }

    //will format current date into this example: Tue,Nov 2, 2021, 12:23 pm
    private fun Date.formatCurrentDate(context : Context) : String {
        return try {
            val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
            //Here you put how you want your date to be, this looks like this Tue,Nov 2, 2021, 12:23 pm
            val outputFormatDay = SimpleDateFormat("dd-MM-yyyy", currentLocale)
            outputFormatDay.timeZone = TimeZone.getDefault()
            val newDateString = outputFormatDay.format(this)
            newDateString
        }catch (_ : Exception){
            "00:00:00"
        }
    }

    private fun String.toDate(dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss'Z'", timeZone: TimeZone = TimeZone.getDefault()): Date? {
        Log.d("LaunchRecyclerAdapter", "timezone: $timeZone ")
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(this )
    }


}