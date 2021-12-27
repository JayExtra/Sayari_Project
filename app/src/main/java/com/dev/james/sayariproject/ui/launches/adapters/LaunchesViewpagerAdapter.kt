package com.dev.james.sayariproject.ui.launches.adapters

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dev.james.sayariproject.ui.launches.PreviousLaunchesFragment
import com.dev.james.sayariproject.ui.launches.UpcomingLaunchesFragment

class LaunchesViewpagerAdapter(
    list : ArrayList<Fragment>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
    ): FragmentStateAdapter(fragmentManager , lifecycle) {
    private val fragmentList = list
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun createFragment(position: Int): Fragment {
        when(position){
            0-> return fragmentList[0]
            1-> return fragmentList[1]
        }
        return fragmentList[0]
    }


}