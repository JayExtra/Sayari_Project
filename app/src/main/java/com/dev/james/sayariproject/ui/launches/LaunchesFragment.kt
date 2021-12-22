package com.dev.james.sayariproject.ui.launches

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.dev.james.sayariproject.databinding.FragmentLaunchesBinding
import com.dev.james.sayariproject.ui.base.BaseFragment
import com.dev.james.sayariproject.ui.launches.adapters.LaunchesViewpagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LaunchesFragment : BaseFragment<FragmentLaunchesBinding>() {
    override val bindingInflater: (LayoutInflater) -> ViewBinding
        get() = FragmentLaunchesBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentList = arrayListOf(
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
        }
    }

    private fun makeInitialLoad() {
        val currentTab = binding.launchesTabLayout.selectedTabPosition
        if(currentTab == 1){
            Log.d("LaunchFragment", "makeInitialLoad: previous tab selected ")
        }else {
            Log.d("LaunchFragment", "makeInitialLoad: upcoming tab selected ")
        }
    }
}