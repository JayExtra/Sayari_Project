package com.dev.james.sayariproject.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.dev.james.sayariproject.BuildConfig
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.ActivityMainBinding
import com.dev.james.sayariproject.ui.activities.viewmodels.MainActivityViewModel
import com.dev.james.sayariproject.ui.dialogs.rating.RatingDialog
import com.dev.james.sayariproject.ui.settings.SettingsFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val activityViewModel : MainActivityViewModel by viewModels()


    @Inject
    lateinit var ratingDialog: RatingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_SayariProject)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun observePreferences() {
        activityViewModel.checkSavedPreferences.observe(this , {
            if(it.nightDarkStatus){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        })
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


}