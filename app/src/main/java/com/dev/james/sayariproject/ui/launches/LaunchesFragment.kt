package com.dev.james.sayariproject.ui.launches

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.dev.james.sayariproject.BuildConfig
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentLaunchesBinding
import com.dev.james.sayariproject.ui.launches.adapters.LaunchesViewpagerAdapter
import com.dev.james.sayariproject.ui.launches.viewmodel.LaunchesViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class LaunchesFragment : Fragment() {

    private var _binding: FragmentLaunchesBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val mLaunchesViewModel : LaunchesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLaunchesBinding.inflate(inflater , container , false)

        //setup controller and navHostFragment
        navController = findNavController()
        appBarConfiguration = AppBarConfiguration(
            navController.graph,
            binding.drawerLayout
        )

        binding.navigationView.setupWithNavController(navController)

        binding.newsTopAppBar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }

        //launchesViewModel.getLaunches(0)
        val fragmentList = arrayListOf<Fragment>(
            UpcomingLaunchesFragment(),
            PreviousLaunchesFragment()
        )

        val adapter = LaunchesViewpagerAdapter(
            fragmentList,
            childFragmentManager,
            lifecycle
        )

        binding.apply {
            launchesViewPager.adapter = adapter
            TabLayoutMediator(launchesTabLayout , launchesViewPager){ tab , position ->
                when(position){
                    0 -> {
                        tab.text = "upcoming"
                    }
                    1 -> {
                        tab.text = "previous"

                    }
                }
            }.attach()

        }

        val searchToggle = binding.newsTopAppBar.menu.findItem(R.id.searchAction)
        val searchView = searchToggle.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                mLaunchesViewModel.receiveQuery(query)
                mLaunchesViewModel.receivePreviousQuery(query)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })


        val navHeader = binding.navigationView.getHeaderView(0)
        val navText = navHeader.findViewById<TextView>(R.id.versionNameTxt)
        navText.text = BuildConfig.VERSION_NAME


        return binding.root

    }



    /** private fun makeInitialLoad() {
        val currentTab = binding.launchesTabLayout.selectedTabPosition
        if(currentTab == 1){
            Log.d("LaunchFragment", "makeInitialLoad: previous tab selected ")
        }else {
            Log.d("LaunchFragment", "makeInitialLoad: upcoming tab selected ")
        }
    }

   searchToggle.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
   override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
   Log.d("LaunchesFrag", "onCreateView: searchview opened ")
   return true

   }

   override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
   Log.d("LaunchesFrag", "onCreateView: searchview closed ")
   return true
   }

   })

   makeInitialLoad()

   launchesTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
   override fun onTabSelected(tab: TabLayout.Tab?) {
   val tabposition = tab?.position

   if(tabposition == 1){
   Log.d("LaunchFragment", "onTabSelected: previous tab selected ")
   }else {
   Log.d("LaunchFragment", "onTabSelected: upcoming tab selected ")
   }
   }
   override fun onTabUnselected(tab: TabLayout.Tab?) {
   val tabposition = tab?.position
   if(tabposition == 1){
   Log.d("LaunchFragment", "onTabUnSelected: previous tab unselected ")
   }else {
   Log.d("LaunchFragment", "onTabUnSelected: upcoming tab unselected ")
   }
   }

   override fun onTabReselected(tab: TabLayout.Tab?) {
   val tabposition = tab?.position
   if(tabposition == 1){
   Log.d("LaunchFragment", "onTabReselected: previous tab selected ")
   }else {
   Log.d("LaunchFragment", "onTabReselected: upcoming tab selected ")
   }
   }

   })
**/
}
