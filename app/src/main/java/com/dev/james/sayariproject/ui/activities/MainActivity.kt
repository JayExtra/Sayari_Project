package com.dev.james.sayariproject.ui.activities

import android.Manifest
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.dev.james.sayariproject.BuildConfig
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.data.broadcast_receivers.phone_reboot.PhoneRebootReceiver
import com.dev.james.sayariproject.data.work_manager.DataSyncWorker
import com.dev.james.sayariproject.data.work_manager.LaunchSchedulerWorker
import com.dev.james.sayariproject.databinding.ActivityMainBinding
import com.dev.james.sayariproject.ui.activities.viewmodels.MainActivityViewModel
import com.dev.james.sayariproject.ui.dialogs.rating.RatingDialog
import com.dev.james.sayariproject.ui.settings.SettingsFragment
import com.dev.james.sayariproject.utilities.SAYARI_UNIQUE_PERIODIC_WORK_REQUEST
import com.dev.james.sayariproject.utilities.SAYARI_UNIQUE_WORK_REQUEST
import com.dev.james.sayariproject.utilities.UPDATE_REQUEST_CODE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.FLEXIBLE

    private val activityViewModel : MainActivityViewModel by viewModels()

    private lateinit var workManager : WorkManager
    private lateinit var dataSyncWorker: PeriodicWorkRequest
    private lateinit var schedulerWorker : OneTimeWorkRequest

    companion object {
        const val TAG = "MainActivity"
        const val MAIN_TOPIC = "communication"
    }



  /*  private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {  isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            askNotificationPermission()
        }
    }*/

    @Inject
    lateinit var ratingDialog: RatingDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_SayariProject)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.registerListener(installStateUpdatedListener)
        }

        checkForAppUpdates()

        subsrcibeToFcmTopic()

        val receiver = ComponentName(this, PhoneRebootReceiver::class.java)
        this.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        styleBottomNavBar()

        observePreferences()


        //setup controller and navHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        appBarConfiguration = AppBarConfiguration(
            navController.graph ,
            binding.drawerLayout
        )

        binding.navigationView.setupWithNavController(navController)
        binding.bottomNavigation.setupWithNavController(navController)

        //show bottom navigation on certain fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.welcomeScreenFragment -> {
                    hideBottomNav()
                    hideTopBar()
                    lockNavDrawer()
                }
                R.id.homeFragment -> {
                    showBottomNav()
                    showTopBar()
                    unlockNavDrawer()

                }
                R.id.launchesFragment -> {
                    hideTopBar()
                    showBottomNav()
                    unlockNavDrawer()

                }
                R.id.searchFragment -> {
                    showBottomNav()
                    showTopBar()
                    unlockNavDrawer()

                }
                R.id.notificationsFragment -> {
                    showBottomNav()
                    showTopBar()
                    unlockNavDrawer()

                }
                R.id.newsFragment -> {
                    hideBottomNav()
                    hideTopBar()
                    lockNavDrawer()
                }
                R.id.eventsFragment2 -> {
                    hideTopBar()
                    lockNavDrawer()
                   hideBottomNav()
                }
                R.id.issFragment2 -> {
                    hideTopBar()
                    lockNavDrawer()
                    hideBottomNav()
                }
                R.id.aboutFragment -> {
                    hideTopBar()
                    lockNavDrawer()
                    hideBottomNav()
                }
                R.id.sendEmailFragment -> {
                    hideTopBar()
                    lockNavDrawer()
                    hideBottomNav()
                }
                R.id.launchDetailsFragment -> {
                    hideTopBar()
                    lockNavDrawer()
                    hideBottomNav()
                }
                R.id.settingsFragment2 -> {
                    hideTopBar()
                    lockNavDrawer()
                    hideBottomNav()
                }
                R.id.supportUsFragment2 -> {
                    hideTopBar()
                    lockNavDrawer()
                    hideBottomNav()
                }
            }
        }

        setSupportActionBar(binding.topAppBar)

        //set up toolbar to open nav drawer
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }
        binding.topAppBar.title = ""

        val navHeader = binding.navigationView.getHeaderView(0)
        val navText = navHeader.findViewById<TextView>(R.id.versionNameTxt)
        navText.text = BuildConfig.VERSION_NAME


    }

    private fun subsrcibeToFcmTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(
            MAIN_TOPIC
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainActivity", "subscribeToFCMTopic: subscribed to topic successfully ")
            } else {
                Log.d("MainActivity", "subscribeToFCMTopic: failed to subscribe to topic")
            }
        }
    }

    /*private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    // FCM SDK (and your app) can post notifications.
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // TODO: display an educational UI explaining to the user the features that will be enabled
                    //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                    //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                    //       If the user selects "No thanks," allow the user to continue without notifications.

                    val rationaleDialog = NotificationPermissionDialog(
                        onRelaunchPermissions = {
                            //ask for permission again
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                    rationaleDialog.show(
                        supportFragmentManager ,
                        NotificationPermissionDialog.TAG
                    )
                }
              *//*  !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED -> {

                    Toast.makeText(this, "Sending to settings screen", Toast.LENGTH_SHORT).show()

                }*//*
                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }*/

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // show snackbar
            Snackbar.make(
                binding.root,
                "Update downloaded successfully. Re-launching the app in 5 seconds",
                Snackbar.LENGTH_LONG
            ).show()

            lifecycleScope.launch {
                delay(5.seconds)
                appUpdateManager.completeUpdate()
            }
        }

        if (state.installStatus() == InstallStatus.FAILED) {

            showDialog(
                title = R.string.update_error_title ,
                message =  R.string.update_install_error_message
            )
        }

        if (state.installStatus() == InstallStatus.CANCELED) {
            // show dialog

            showDialog(
                title = R.string.update_install_warning ,
                message = R.string.update_install_warning_message
            )
        }
    }

    private fun checkForAppUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when (updateType) {
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }

            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    private fun showDialog(title : Int, message : Int){
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Okay"
            ) { dialog, _ -> dialog.dismiss() }
            .show()
    }
    private fun observePreferences() {
        activityViewModel.checkSavedPreferences.observe(this) {
            if (it.nightDarkStatus) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }


    private fun styleBottomNavBar() {
        binding.apply {
            val radius = 25F
            val shapeDrawable : MaterialShapeDrawable = bottomNavigation.background as MaterialShapeDrawable
            shapeDrawable.shapeAppearanceModel = shapeDrawable.shapeAppearanceModel
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu , menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.settings_fragment -> {
      //          Log.d("MainActivity", "onOptionsItemSelected: settings selected ")
                navController.navigate(R.id.settingsFragment2)
                true
            }
            R.id.rate_us -> {
                ratingDialog.show(supportFragmentManager , null)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }


    private fun hideBottomNav(){
        binding.bottomNavigation.visibility = View.GONE
    }

    private fun showBottomNav(){
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    private fun hideTopBar(){
        binding.topAppBar.visibility = View.GONE
    }
    private fun showTopBar(){
        binding.topAppBar.visibility = View.VISIBLE
    }

    private fun lockNavDrawer(){
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

    }

    private fun unlockNavDrawer(){
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }


    override fun onResume() {
        super.onResume()
        initWorkProcess()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Timber.tag("MainActivity").d("Something went wrong with updating the app.")
            }
            if (resultCode == RESULT_CANCELED) {
                Timber.tag("MainActivity").d("Update cancelled")
            }
        }
    }



    private fun initWorkProcess(){
        Log.d(TAG, "initWorkProcess: work manager workers initialized ")
        workManager = WorkManager.getInstance(applicationContext)
        dataSyncWorker = PeriodicWorkRequestBuilder<DataSyncWorker>(1 , TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        schedulerWorker = OneTimeWorkRequestBuilder<LaunchSchedulerWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SAYARI_UNIQUE_PERIODIC_WORK_REQUEST ,
            ExistingPeriodicWorkPolicy.KEEP,
            dataSyncWorker
        )

        workManager.beginUniqueWork(
            SAYARI_UNIQUE_WORK_REQUEST ,
            ExistingWorkPolicy.KEEP,
            schedulerWorker
        ).enqueue()

        observeWorkerStatus()
    }

    private fun observeWorkerStatus() {
        workManager.getWorkInfosForUniqueWorkLiveData(SAYARI_UNIQUE_WORK_REQUEST).observe(this) {
            val syncInfo = it.find { syncInf ->
                syncInf.id == dataSyncWorker.id
            }
            val schedulerInfo = it.find { schedulerInf ->
                schedulerInf.id == schedulerWorker.id
            }

            when(syncInfo?.state){
                WorkInfo.State.RUNNING ->{
                    Log.d("MainActivity", "observeWorkerStatus: data sync worker => running... ")
                }
                WorkInfo.State.SUCCEEDED -> {
                    Log.d("MainActivity", "observeWorkerStatus: data sync worker => succeeded!... ")
                }
                WorkInfo.State.FAILED -> {
                    Log.d("MainActivity", "observeWorkerStatus: data sync worker => failed! ")
                }
                WorkInfo.State.ENQUEUED -> {
                    Log.d("MainActivity", "observeWorkerStatus: data sync worker => enqueued.. ")
                }
                WorkInfo.State.CANCELLED -> {
                    Log.d("MainActivity", "observeWorkerStatus: data sync worker => cancelled! ")
                }
                WorkInfo.State.BLOCKED -> Log.d("MainActivity", "observeWorkerStatus: data sync worker => blocked ")

                else -> Log.d("MainActivity", "data sync worker: some error , I don't know")

            }

            when(schedulerInfo?.state){
                WorkInfo.State.RUNNING ->{
                    Log.d("MainActivity", "observeWorkerStatus: scheduler worker => running... ")
                }
                WorkInfo.State.SUCCEEDED -> {
                    Log.d("MainActivity", "observeWorkerStatus: scheduler worker => succeeded! ")
                }
                WorkInfo.State.FAILED -> {
                    Log.d("MainActivity", "observeWorkerStatus: scheduler worker => failed! ")
                }
                WorkInfo.State.ENQUEUED -> {
                    Log.d("MainActivity", "observeWorkerStatus: scheduler worker => enqueued.. ")
                }
                WorkInfo.State.CANCELLED -> {
                    Log.d("MainActivity", "observeWorkerStatus: scheduler worker => cancelled! ")
                }
                WorkInfo.State.BLOCKED -> Log.d("MainActivity", "observeWorkerStatus: scheduler worker => blocked ")

                else -> Log.d("MainActivity", "scheduler worker: some error , I don't know")

            }
        }
    }


}