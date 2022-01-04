package com.dev.james.sayariproject.ui.launches

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentLaunchesBinding
import com.dev.james.sayariproject.ui.launches.adapters.LaunchesViewpagerAdapter
import com.dev.james.sayariproject.ui.launches.viewmodel.LaunchesViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class LaunchesFragment : Fragment() {

    private var _binding: FragmentLaunchesBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val mLaunchesViewModel : LaunchesViewModel by activityViewModels()

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

        binding.launchesTopAppBar.setNavigationOnClickListener {
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

            /**  makeInitialLoad()

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

            })**/
        }

        val searchToggle = binding.launchesTopAppBar.menu.findItem(R.id.searchAction)
        val searchView = searchToggle.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                mLaunchesViewModel.receiveQuery(query)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })

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
**/
}
