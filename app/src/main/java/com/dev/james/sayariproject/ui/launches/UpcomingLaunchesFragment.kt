package com.dev.james.sayariproject.ui.launches

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.ExperimentalPagingApi
import com.dev.james.sayariproject.databinding.FragmentUpcomingPreviousLaunchesBinding
import com.dev.james.sayariproject.ui.launches.viewmodel.LaunchesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpcomingLaunchesFragment : Fragment() {
    private lateinit var binding: FragmentUpcomingPreviousLaunchesBinding
    @ExperimentalPagingApi
    private val upcomingViewModel : LaunchesViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUpcomingPreviousLaunchesBinding.bind(view)
    }
}