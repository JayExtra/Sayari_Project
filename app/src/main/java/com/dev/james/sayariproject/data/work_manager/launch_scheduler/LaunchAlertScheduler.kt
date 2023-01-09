package com.dev.james.sayariproject.data.work_manager.launch_scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.os.ConfigurationCompat
import com.dev.james.sayariproject.data.broadcast_receivers.events.EventsAlertsReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.launch.FifteenMinuteLaunchAlertReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.launch.FiveMinuteLaunchAlertReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.launch.LaunchWindowOpenAlertReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.launch.ThirtyMinuteLaunchAlertReceiver
import com.dev.james.sayariproject.data.broadcast_receivers.midninight.MidnightAlertReceiver
import com.dev.james.sayariproject.data.local.datastore.DataStoreManager
import com.dev.james.sayariproject.data.local.datastore.DatastorePreferenceKeys
import com.dev.james.sayariproject.data.local.room.Dao
import com.dev.james.sayariproject.models.events.Events
import com.dev.james.sayariproject.models.launch.LaunchManifestItem
import com.dev.james.sayariproject.utilities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

object TimeConstants {
    const val THIRTY_MINUTE_DIFFERENCE = 1800000
    const val FIFTEEN_MINUTE_DIFFERENCE = 900000
    const val FIVE_MINUTE_DIFFERENCE = 300000
}

class LaunchAlertScheduler @Inject constructor(
    private val dao : Dao,
    @ApplicationContext private val context : Context,
    private val dataStoreManager: DataStoreManager
    ) : BaseLaunchScheduler {

    private val currentDate = Calendar.getInstance().time
    private val alarmManager : AlarmManager = context.getSystemService(AlarmManager::class.java)
   /* private val fakeManifestItemsList = listOf(
        LaunchManifestItem(
            id = "ojhcss9837904" ,
            slug = "ARBATOS-11" ,
            name = "Arbatos first test flight" ,
            window_start = "2023-01-09T15:10:00Z"
        )
    )*/

    override suspend fun initScheduler() {
        //1. fetches the launch manifest list from the database and returns launches
        //happening today

        val launchManifest = dao.getLaunchManifest().filter { launchItem ->
            launchItem.window_start.formatDateFromApi(context) == currentDate.formatCurrentDate(context)
        }

        Log.d("LaunchAlertScheduler", "initScheduler: available launches => $launchManifest")

        //2. schedules alarms available during that day
        if(launchManifest.isEmpty()) {
            Log.d("LaunchAlertScheduler", "scheduleUpcomingLaunches: no launch found")
        }else {
            scheduleUpcomingLaunches(launchManifest)
        }
        //3. sets the midnight alert for the next day to trigger the initScheduler again
        scheduleMidnightAlarm()

        //activatePhoneRebootReceiver()
    }

    private fun scheduleMidnightAlarm(){

        //Set the alarm to start at approximately 00:00 a.m
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY , 0)
        calendar.set(Calendar.MINUTE , 0)
        calendar.set(Calendar.SECOND , 0)
        calendar.add(Calendar.DATE , 1)


        val intent = Intent(context, MidnightAlertReceiver::class.java )
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                MIDNIGHT_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val triggerTime = calendar.timeInMillis + 60000
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime ,
            pendingIntent
        )

        Log.d("LaunchAlertScheduler", "midnight alarm scheduled for $triggerTime =>  ${triggerTime.convertToReadableTime()} ")

    }

    //will schedule alarms for launches happening in that day
    private suspend fun scheduleUpcomingLaunches(launchManifest: List<LaunchManifestItem>) {

        Log.d("LaunchAlertScheduler", "scheduleUpcomingLaunches: available ${launchManifest.size} ")

        launchManifest.forEach { launch ->
           //set alarm
           setAlarm(launch.window_start , launch.name , launch.slug , launch.id)

           //update the manifest
           deleteFromManifest(launch)

       }

        //check if launch manifest is healthy i.e launch items are greater than 10
        // otherwise refresh the launch manifest
        //checkLaunchManifest()

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
            Log.d("LaunchAlertScheduler", "thirty minute preference detected")
          setThirtyMinuteAlarm(
              windowStart = windowStart ,
              name = name ,
              id = id ,
              slug = slug)
        }
        if(fifteenMinuteSet){
            Log.d("LaunchAlertScheduler", "fifteen minute preference detected")
            setFifteenMinuteAlarm(
                windowStart = windowStart ,
                name = name ,
                id = id ,
                slug = slug)
        }
        if (fiveMinuteSet){
            Log.d("LaunchAlertScheduler", "five minute preference detected")
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
        val intent = Intent(context , LaunchWindowOpenAlertReceiver::class.java).apply {
            putExtra("launch_id" , id )
            putExtra("launch_name" , name)
            putExtra("launch_slug" , slug)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context ,
            generateUniqueId(),
            intent ,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (date != null) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                date ,
                pendingIntent)
            Log.d("LaunchAlertScheduler", "launch alert set for: ${windowStart.toDate()}")
        }
    }

    override suspend fun setEventAlarm(event: Events){
        val date = event.date.toDate()?.time
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context , EventsAlertsReceiver::class.java).apply {
            putExtra("event_id" , event.id)
            putExtra("event_name" , event.name)
            putExtra("event_slug" , event.slug)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context ,
            generateUniqueId() ,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if(date != null){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP , date , pendingIntent)
            Log.d("LaunchAlertScheduler", "launch alert set for: ${event.date.toDate()}")
        }
    }



    private fun setThirtyMinuteAlarm(windowStart : String , name: String , id: String , slug: String){
        val alertTime = windowStart.toDate()?.time
        Log.d("LaunchAlertScheduler", "setAlarm: 30 min before : ${alertTime?.convertToReadableTime()}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context , ThirtyMinuteLaunchAlertReceiver::class.java).apply {
            putExtra("launch_id" , id )
            putExtra("launch_name" , name)
            putExtra("launch_slug" , slug)
        }

        val pendingIntent = PendingIntent.getBroadcast(context,
            generateUniqueId(),
            intent ,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (alertTime != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP , alertTime - TimeConstants.THIRTY_MINUTE_DIFFERENCE  , pendingIntent)
            Log.d("LaunchAlertScheduler",
                "launch alert set for: ${windowStart.toDate()} thirty minutes before => ${(alertTime - TimeConstants.THIRTY_MINUTE_DIFFERENCE).convertToReadableTime()}")
        }
    }


    private fun setFifteenMinuteAlarm(windowStart : String , name: String , id: String , slug: String){
        val alertTime = windowStart.toDate()?.time
        Log.d("LaunchAlertScheduler", "setAlarm: 15 min before : ${alertTime?.let { alertTime.convertToReadableTime() }}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context , FifteenMinuteLaunchAlertReceiver::class.java).apply {
            putExtra("launch_id" , id )
            putExtra("launch_name" , name)
            putExtra("launch_slug" , slug)
        }

        val pendingIntent = PendingIntent.getBroadcast(context ,
            generateUniqueId() ,
            intent ,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (alertTime != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP , alertTime - TimeConstants.FIFTEEN_MINUTE_DIFFERENCE , pendingIntent)
            Log.d("LaunchAlertScheduler",
                "launch alert set for: ${windowStart.toDate()} fifteen minute adjustment => ${(alertTime - TimeConstants.FIFTEEN_MINUTE_DIFFERENCE).convertToReadableTime()}"
            )
        }
    }



    private fun setFiveMinuteAlarm(windowStart : String , name: String , id: String , slug: String){
        val alertTime = windowStart.toDate()?.time
        Log.d("LaunchAlertScheduler", "setAlarm: 15 min added : ${alertTime?.let { alertTime.convertToReadableTime() }}")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context , FiveMinuteLaunchAlertReceiver::class.java).apply {
            putExtra("launch_id" , id )
            putExtra("launch_name" , name)
            putExtra("launch_slug" , slug)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context ,
           generateUniqueId() ,
            intent ,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (alertTime != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP , alertTime - TimeConstants.FIVE_MINUTE_DIFFERENCE , pendingIntent)
            Log.d("LaunchAlertScheduler", "launch alert set for: ${windowStart.toDate()} five minute adjustment => ${(alertTime - TimeConstants.FIVE_MINUTE_DIFFERENCE).convertToReadableTime()}")
        }
    }

   /* private suspend fun checkLaunchManifest(){
        val launchListSize = dao.getLaunchManifest().size
        if(launchListSize<= 10) {
            Log.d("LaunchAlertScheduler", "The manifest is not healthy. Launch list size => $launchListSize")
        }else {
            Log.d("LaunchAlertScheduler", "The manifest is healthy. Launch list size => $launchListSize")
        }
    }*/

    /*private fun activatePhoneRebootReceiver(){
        val receiver = ComponentName(context, PhoneRebootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }*/

    //will format the date from API into local time
    private fun String.formatDateFromApi(context : Context) : String {
        return try {
            val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("GMT")
            val passedDate: Date = inputFormat.parse(this) as Date

            //Here you put how you want your date to be, this looks like this Tue,Nov 2, 2021
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
            //Here you put how you want your date to be, this looks like this Tue,Nov 2, 2021
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

    private fun generateUniqueId(): Int {
        val uniqueSets = mutableSetOf<Int>()
        val min = 1
        val max = 10000
        val random = Random()
        var id = random.nextInt(max - min + 1) + min
        while (id in uniqueSets) {
            id = random.nextInt(max - min + 1) + min
        }
        uniqueSets.add(id)
        if(uniqueSets.size >= 10000) uniqueSets.clear()
        return id
    }

    private fun Long.convertToReadableTime() : String {
        return SimpleDateFormat("HH:mm:ss").format(Date(this))
    }


}