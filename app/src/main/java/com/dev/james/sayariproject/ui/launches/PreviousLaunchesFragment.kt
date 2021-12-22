package com.dev.james.sayariproject.ui.launches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.FragmentUpcomingPreviousLaunchesBinding
import com.dev.james.sayariproject.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviousLaunchesFragment : Fragment() {
    private lateinit var binding: FragmentUpcomingPreviousLaunchesBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUpcomingPreviousLaunchesBinding.bind(view)
    }
}