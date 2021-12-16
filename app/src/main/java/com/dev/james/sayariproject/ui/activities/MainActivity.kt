package com.dev.james.sayariproject.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.ActivityMainBinding
import com.dev.james.sayariproject.ui.welcome.viewmodel.WelcomeScreenViewModel
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel : WelcomeScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_SayariProject)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        styleBottomNavBar()


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
                    showBottomNav()
                    showTopBar()
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
            }
        }


        //set up toolbar to open nav drawer
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.open()
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