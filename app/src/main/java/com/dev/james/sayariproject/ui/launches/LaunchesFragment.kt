package com.dev.james.sayariproject.ui.launches

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.room.RoomDatabase
import androidx.viewbinding.ViewBinding
import com.dev.james.sayariproject.databinding.FragmentLaunchesBinding
import com.dev.james.sayariproject.databinding.FragmentUpcomingLaunchesBinding
import com.dev.james.sayariproject.ui.activities.viewmodels.SharedViewModel
import com.dev.james.sayariproject.ui.base.BaseFragment
import com.dev.james.sayariproject.ui.launches.adapters.LaunchesViewpagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class LaunchesFragment : Fragment() {

    private var _binding: FragmentLaunchesBinding? = null
    private val binding get() = _binding!!


    private val sharedViewModel : SharedViewModel by activityViewModels()

    var mCommunicator : QuerySelectedListener? = null


    interface QuerySelectedListener {
        fun sendQueryToFragment(query : String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCommunicator = context as QuerySelectedListener
    }

    override fun onDestroy() {
        super.onDestroy()
        mCommunicator = null
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLaunchesBinding.inflate(inflater , container , false)

        sharedViewModel.queryPassed.observe(viewLifecycleOwner , Observer { event ->
            event.getContentIfNotHandled()?.let { query ->
                Log.d("LaunchFragment", "query received: $query")
                mCommunicator?.sendQueryToFragment(query)
            }
        })

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

        return binding.root

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
