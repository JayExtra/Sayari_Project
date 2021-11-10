package com.dev.james.sayariproject.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.ActivityMainBinding
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_SayariProject)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        styleBottomNavBar()
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.launchesFragment,
                R.id.searchFragment,
                R.id.notificationsFragment
            )
        )

        //setup controller and navHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        binding.bottomNavigation.setupWithNavController(navController)

        //show bottom navigation on certain fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.welcomeScreenFragment -> hideBottomNav()
                R.id.homeFragment -> showBottomNav()
                R.id.launchesFragment -> showBottomNav()
                R.id.searchFragment -> showBottomNav()
                R.id.notificationsFragment -> showBottomNav()
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

    private fun hideBottomNav(){
        binding.bottomNavigation.visibility = View.INVISIBLE
    }

    private fun showBottomNav(){
        binding.bottomNavigation.visibility = View.VISIBLE
    }
}